package id.aisnext.organization.infrastructure;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.organization.api.SchoolLevel;
import id.aisnext.organization.api.SchoolType;
import id.aisnext.organization.api.SchoolTypeCommand;
import id.aisnext.organization.api.SchoolTypeSort;
import id.aisnext.organization.domain.SchoolTypeConflictException;
import id.aisnext.organization.domain.SchoolTypeNotFoundException;
import id.aisnext.organization.domain.SchoolTypeRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Parameterized JDBC adapter for the legacy school-type table and its Envers audit tables.
 *
 * <p>Every mutating method expects an ambient CORE transaction. It never creates, alters, or drops
 * tenant schema objects.</p>
 */
@Repository
public class JdbcSchoolTypeRepository implements SchoolTypeRepository {
    private static final String SELECT_COLUMNS = """
            select js.id, js.nama, js.jenjang, coalesce(j.nama, '') level_name,
                   js.keterangan, coalesce(js.aktif, true) active,
                   js.oleh, js.olehid, js.tanggal_dirubah
              from sekolah.jenis_sekolah js
              left join public.jenjang j on j.id = js.jenjang
            """;

    private final JdbcClient core;

    /**
     * Creates the adapter with the tenant-routed CORE JDBC client.
     *
     * @param core tenant-aware client selected only from trusted {@code TenantContext}
     */
    public JdbcSchoolTypeRepository(@Qualifier("coreJdbcClient") JdbcClient core) {
        this.core = core;
    }

    /**
     * Filters, sorts, and pages rows using bound values and a closed sort whitelist.
     *
     * @param query validated paging and filter input
     * @param sort whitelisted sort option
     * @param activeOnly whether inactive rows are excluded
     * @return deterministic result page
     */
    @Override
    public PageResult<SchoolType> findAll(PageQuery query, SchoolTypeSort sort, boolean activeOnly) {
        String pattern = "%" + query.filter().toLowerCase(java.util.Locale.ROOT) + "%";
        String predicate = "lower(js.nama) like :pattern and (:activeOnly = false or coalesce(js.aktif, true))";
        long total = core.sql("select count(*) from sekolah.jenis_sekolah js where " + predicate)
                .param("pattern", pattern).param("activeOnly", activeOnly).query(Long.class).single();
        List<SchoolType> rows = core.sql(SELECT_COLUMNS + " where " + predicate + " order by "
                        + orderBy(sort) + " limit :limit offset :offset")
                .param("pattern", pattern).param("activeOnly", activeOnly)
                .param("limit", query.size()).param("offset", query.offset())
                .query(this::map).list();
        return new PageResult<>(rows, query.page(), query.size(), total);
    }

    /**
     * Finds one row by its exact legacy identifier.
     *
     * @param id legacy primary key
     * @return row projection, or empty when absent
     */
    @Override
    public Optional<SchoolType> findById(long id) {
        return core.sql(SELECT_COLUMNS + " where js.id = :id")
                .param("id", id).query(this::map).optional();
    }

    /**
     * Lists valid choices while retaining inactive levels already used by existing rows.
     *
     * @return ordered level projections
     */
    @Override
    public List<SchoolLevel> findSelectableLevels() {
        return core.sql("""
                select j.id, coalesce(j.nama, '') name, coalesce(j.aktif, true) active
                  from public.jenjang j
                 where coalesce(j.aktif, true)
                    or exists (select 1 from sekolah.jenis_sekolah js where js.jenjang = j.id)
                 order by lower(coalesce(j.nama, '')), j.id
                """).query((resultSet, rowNumber) -> new SchoolLevel(
                        resultSet.getLong("id"), resultSet.getString("name"), resultSet.getBoolean("active")))
                .list();
    }

    /**
     * Checks the foreign-key target without modifying it.
     *
     * @param levelId level identifier
     * @return whether the row exists
     */
    @Override
    public boolean levelExists(long levelId) {
        return core.sql("select exists(select 1 from public.jenjang where id = :id)")
                .param("id", levelId).query(Boolean.class).single();
    }

    /**
     * Applies the legacy form's exact case-sensitive uniqueness comparison.
     *
     * @param name normalized submitted name
     * @param excludedId current row identifier during update, or null during create
     * @return whether another row conflicts
     */
    @Override
    public boolean nameExists(String name, Long excludedId) {
        if (excludedId == null) {
            return core.sql("select exists(select 1 from sekolah.jenis_sekolah where nama = :name)")
                    .param("name", name).query(Boolean.class).single();
        }
        return core.sql("""
                select exists(select 1 from sekolah.jenis_sekolah
                               where nama = :name and id <> :excludedId)
                """).param("name", name).param("excludedId", excludedId).query(Boolean.class).single();
    }

    /**
     * Inserts the business row, then emits an Envers create snapshot in the same transaction.
     *
     * @param command validated create command
     * @param actorId authenticated user identifier
     * @return created row projection
     */
    @Override
    public SchoolType create(SchoolTypeCommand command, String actorId) {
        String actorName = actorName(actorId);
        long id = core.sql("""
                insert into sekolah.jenis_sekolah
                    (nama, jenjang, keterangan, aktif, oleh, olehid, tanggal_dirubah)
                values (:name, :levelId, :description, :active, :actorName, :actorId, localtimestamp)
                returning id
                """).param("name", command.name()).param("levelId", command.levelId())
                .param("description", command.description()).param("active", command.active())
                .param("actorName", actorName).param("actorId", actorId).query(Long.class).single();
        SchoolType created = require(id, false);
        appendAudit(created, (short) 0);
        return created;
    }

    /**
     * Locks the row, rejects a stale token, updates physical columns, and emits Envers revision 1.
     *
     * @param id legacy primary key
     * @param versionToken expected snapshot token
     * @param command validated update command
     * @param actorId authenticated user identifier
     * @return updated row projection
     */
    @Override
    public SchoolType update(long id, String versionToken, SchoolTypeCommand command, String actorId) {
        SchoolType current = require(id, true);
        requireCurrentVersion(current, versionToken);
        core.sql("""
                update sekolah.jenis_sekolah
                   set nama = :name, jenjang = :levelId, keterangan = :description,
                       aktif = :active, oleh = :actorName, olehid = :actorId,
                       tanggal_dirubah = localtimestamp
                 where id = :id
                """).param("name", command.name()).param("levelId", command.levelId())
                .param("description", command.description()).param("active", command.active())
                .param("actorName", actorName(actorId)).param("actorId", actorId).param("id", id).update();
        SchoolType updated = require(id, false);
        appendAudit(updated, (short) 1);
        return updated;
    }

    /**
     * Locks and deletes only an unreferenced current row, then emits Envers revision 2.
     *
     * @param id legacy primary key
     * @param versionToken expected snapshot token
     * @param actorId authenticated user identifier retained for the command audit boundary
     */
    @Override
    public void delete(long id, String versionToken, String actorId) {
        SchoolType current = require(id, true);
        requireCurrentVersion(current, versionToken);
        long references = core.sql("select count(*) from sekolah.sekolah where jenis_sekolah_id = :id")
                .param("id", id).query(Long.class).single();
        if (references > 0) {
            throw new SchoolTypeConflictException(
                    "Jenis sekolah masih dipakai oleh " + references + " sekolah dan tidak dapat dihapus");
        }
        core.sql("delete from sekolah.jenis_sekolah where id = :id").param("id", id).update();
        appendAudit(current, (short) 2);
    }

    /**
     * Loads a row with an optional PostgreSQL row lock.
     *
     * @param id legacy primary key
     * @param lock whether to append {@code FOR UPDATE OF js}
     * @return existing row projection
     * @throws SchoolTypeNotFoundException when no row exists
     */
    private SchoolType require(long id, boolean lock) {
        return core.sql(SELECT_COLUMNS + " where js.id = :id" + (lock ? " for update of js" : ""))
                .param("id", id).query(this::map).optional()
                .orElseThrow(() -> new SchoolTypeNotFoundException(id));
    }

    /**
     * Resolves a stable writer label while preserving the principal ID when no name is available.
     *
     * @param actorId authenticated legacy user identifier
     * @return nonblank display name
     */
    private String actorName(String actorId) {
        return core.sql("select coalesce(nullif(btrim(usernama), ''), userid) from tbmuser where userid = :id")
                .param("id", actorId).query(String.class).optional().orElse(actorId);
    }

    /**
     * Inserts a shared legacy revision and full entity snapshot using Envers numeric semantics.
     *
     * @param value entity snapshot written to the audit mirror
     * @param revisionType zero=create, one=update, or two=delete
     */
    private void appendAudit(SchoolType value, short revisionType) {
        long revision = core.sql("select nextval('public.hibernate_sequence')")
                .query(Long.class).single();
        long timestamp = core.sql("select floor(extract(epoch from clock_timestamp()) * 1000)::bigint")
                .query(Long.class).single();
        core.sql("insert into new_audit.revinfo (rev, revtstmp) values (:rev, :timestamp)")
                .param("rev", revision).param("timestamp", timestamp).update();
        core.sql("""
                insert into new_audit.jenis_sekolah__audit
                    (id, rev, revtype, aktif, keterangan, nama, oleh, olehid, tanggal_dirubah, jenjang)
                values (:id, :rev, :type, :active, :description, :name, :changedBy,
                        :changedById, :changedAt, :levelId)
                """).param("id", value.id()).param("rev", revision).param("type", revisionType)
                .param("active", value.active()).param("description", value.description())
                .param("name", value.name()).param("changedBy", value.changedBy())
                .param("changedById", value.changedById()).param("changedAt", value.changedAt())
                .param("levelId", value.levelId()).update();
    }

    /**
     * Rejects a lost update or delete using a constant-time comparison of opaque tokens.
     *
     * @param current locked current row
     * @param submittedToken caller's expected token
     * @throws SchoolTypeConflictException when the token is absent or stale
     */
    private static void requireCurrentVersion(SchoolType current, String submittedToken) {
        boolean equal = submittedToken != null && java.security.MessageDigest.isEqual(
                current.versionToken().getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                submittedToken.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        if (!equal) {
            throw new SchoolTypeConflictException(
                    "Data telah berubah sejak dibuka; muat ulang sebelum menyimpan");
        }
    }

    /**
     * Maps one joined row and derives its concurrency token from physical mutable values.
     *
     * @param resultSet positioned JDBC result set
     * @param rowNumber zero-based row index supplied by Spring
     * @return immutable school-type projection
     * @throws SQLException when a JDBC value cannot be read
     */
    private SchoolType map(ResultSet resultSet, int rowNumber) throws SQLException {
        Long levelId = resultSet.getObject("jenjang", Long.class);
        LocalDateTime changedAt = resultSet.getObject("tanggal_dirubah", LocalDateTime.class);
        SchoolType value = new SchoolType(resultSet.getLong("id"), resultSet.getString("nama"),
                levelId, resultSet.getString("level_name"), resultSet.getString("keterangan"),
                resultSet.getBoolean("active"), resultSet.getString("oleh"),
                resultSet.getString("olehid"), changedAt, "");
        return new SchoolType(value.id(), value.name(), value.levelId(), value.levelName(),
                value.description(), value.active(), value.changedBy(), value.changedById(),
                value.changedAt(), SchoolTypeVersion.token(value));
    }

    /**
     * Converts a closed enum value to a trusted SQL ordering fragment.
     *
     * @param sort whitelisted sort option
     * @return hard-coded SQL fragment with a deterministic identifier tie-breaker
     */
    private static String orderBy(SchoolTypeSort sort) {
        return switch (sort) {
            case NAME_ASC -> "lower(js.nama) asc, js.id asc";
            case NAME_DESC -> "lower(js.nama) desc, js.id desc";
            case ID_ASC -> "js.id asc";
            case ID_DESC -> "js.id desc";
        };
    }
}
