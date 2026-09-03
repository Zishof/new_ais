package id.aisnext.organization.application;

import id.aisnext.kernel.api.PageQuery;
import id.aisnext.kernel.api.PageResult;
import id.aisnext.organization.api.SchoolLevel;
import id.aisnext.organization.api.SchoolType;
import id.aisnext.organization.api.SchoolTypeCommand;
import id.aisnext.organization.api.SchoolTypeSort;
import id.aisnext.organization.domain.SchoolTypeConflictException;
import id.aisnext.organization.domain.SchoolTypeNotFoundException;
import id.aisnext.organization.domain.SchoolTypeRepository;
import id.aisnext.organization.domain.SchoolTypeValidationException;
import id.aisnext.tenant.api.TenantContext;
import id.aisnext.tenant.api.TenantWritePolicy;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application boundary for the clone-gated school-type master-data slice.
 *
 * <p>Reads preserve legacy null-active behavior. Every command checks current control-plane
 * ownership and executes business plus Envers writes in one tenant CORE transaction.</p>
 */
@Service
public class SchoolTypeService {
    /** Stable control-plane key for the audited aggregate. */
    public static final String AGGREGATE_KEY = "organization.jenis-sekolah";

    private final SchoolTypeRepository repository;
    private final TenantWritePolicy writePolicy;

    /**
     * Creates the application service from persistence and control-plane boundaries.
     *
     * @param repository legacy-compatible school-type persistence port
     * @param writePolicy fail-closed tenant aggregate ownership policy
     */
    public SchoolTypeService(SchoolTypeRepository repository, TenantWritePolicy writePolicy) {
        this.repository = repository;
        this.writePolicy = writePolicy;
    }

    /**
     * Returns a validated page of school types.
     *
     * @param page zero-based page index
     * @param size requested page size from 1 through 200
     * @param filter case-insensitive name fragment
     * @param sort whitelisted sort option
     * @param activeOnly whether inactive rows are excluded
     * @return deterministic result page
     */
    @Transactional(transactionManager = "coreTransactionManager", readOnly = true)
    public PageResult<SchoolType> findAll(int page, int size, String filter,
                                          SchoolTypeSort sort, boolean activeOnly) {
        return repository.findAll(new PageQuery(page, size, filter), sort, activeOnly);
    }

    /**
     * Finds one school type without acquiring a write lock.
     *
     * @param id legacy primary key
     * @return projection, or empty when absent
     */
    @Transactional(transactionManager = "coreTransactionManager", readOnly = true)
    public Optional<SchoolType> findOne(long id) {
        return repository.findById(id);
    }

    /**
     * Lists level choices that keep all existing legacy rows editable.
     *
     * @return active or currently referenced education levels
     */
    @Transactional(transactionManager = "coreTransactionManager", readOnly = true)
    public List<SchoolLevel> selectableLevels() {
        return repository.findSelectableLevels();
    }

    /**
     * Creates one school type after ownership and business validation.
     *
     * @param command untrusted submitted fields
     * @param actorId authenticated legacy user identifier
     * @return created row including its current concurrency token
     * @throws SchoolTypeValidationException when required fields or lengths are invalid
     * @throws SchoolTypeConflictException when an exact normalized name already exists
     */
    @Transactional(transactionManager = "coreTransactionManager")
    public SchoolType create(SchoolTypeCommand command, String actorId) {
        requireNextWrite();
        SchoolTypeCommand normalized = normalize(command);
        requireUniqueName(normalized.name(), null);
        return repository.create(normalized, requireActor(actorId));
    }

    /**
     * Updates one current row after ownership, optimistic-concurrency, and business validation.
     *
     * @param id legacy primary key
     * @param versionToken expected current snapshot token
     * @param command untrusted submitted fields
     * @param actorId authenticated legacy user identifier
     * @return updated row and replacement concurrency token
     * @throws SchoolTypeNotFoundException when the row is missing
     * @throws SchoolTypeConflictException when the token is stale or name already exists
     * @throws SchoolTypeValidationException when submitted fields are invalid
     */
    @Transactional(transactionManager = "coreTransactionManager")
    public SchoolType update(long id, String versionToken, SchoolTypeCommand command, String actorId) {
        requireNextWrite();
        SchoolTypeCommand normalized = normalize(command);
        requireUniqueName(normalized.name(), id);
        return repository.update(id, versionToken, normalized, requireActor(actorId));
    }

    /**
     * Deletes one current, unreferenced row after checking tenant write ownership.
     *
     * @param id legacy primary key
     * @param versionToken expected current snapshot token
     * @param actorId authenticated legacy user identifier
     * @throws SchoolTypeNotFoundException when the row is missing
     * @throws SchoolTypeConflictException when the row is stale or referenced by a school
     */
    @Transactional(transactionManager = "coreTransactionManager")
    public void delete(long id, String versionToken, String actorId) {
        requireNextWrite();
        repository.delete(id, versionToken, requireActor(actorId));
    }

    /** Requires the current trusted tenant to designate AIS Next as the sole writer. */
    private void requireNextWrite() {
        writePolicy.requireNextWrite(TenantContext.require().id(), AGGREGATE_KEY);
    }

    /**
     * Validates, trims, and canonicalizes fields before any SQL mutation occurs.
     *
     * @param command submitted command
     * @return validated normalized command
     * @throws SchoolTypeValidationException when required fields, foreign keys, or lengths fail
     */
    private SchoolTypeCommand normalize(SchoolTypeCommand command) {
        if (command == null) throw new SchoolTypeValidationException("Data jenis sekolah wajib diisi");
        String name = command.name() == null ? "" : command.name().trim();
        if (name.isEmpty()) throw new SchoolTypeValidationException("Nama jenis sekolah wajib diisi");
        if (name.length() > 255) throw new SchoolTypeValidationException("Nama maksimal 255 karakter");
        if (command.levelId() == null) throw new SchoolTypeValidationException("Jenjang wajib dipilih");
        if (!repository.levelExists(command.levelId())) {
            throw new SchoolTypeValidationException("Jenjang yang dipilih tidak ditemukan");
        }
        String description = command.description() == null ? null : command.description().trim();
        if (description != null && description.isEmpty()) description = null;
        if (description != null && description.length() > 255) {
            throw new SchoolTypeValidationException("Keterangan maksimal 255 karakter");
        }
        return new SchoolTypeCommand(name, command.levelId(), description, command.active());
    }

    /**
     * Enforces the exact, case-sensitive legacy form uniqueness behavior.
     *
     * @param name normalized name
     * @param excludedId current row during update, or null during create
     * @throws SchoolTypeConflictException when another row has the exact same name
     */
    private void requireUniqueName(String name, Long excludedId) {
        if (repository.nameExists(name, excludedId)) {
            throw new SchoolTypeConflictException("Nama jenis sekolah sudah ada di database");
        }
    }

    /**
     * Rejects an absent actor so audit fields can never be silently blanked.
     *
     * @param actorId authenticated principal name
     * @return trimmed actor identifier
     * @throws SchoolTypeValidationException when no authenticated identifier is available
     */
    private static String requireActor(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            throw new SchoolTypeValidationException("Identitas pengguna tidak tersedia");
        }
        return actorId.trim();
    }
}
