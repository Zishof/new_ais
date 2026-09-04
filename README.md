# AIS Next

AIS Next adalah modular monolith Java 25 untuk memodernisasi AIS secara bertahap tanpa memutus
operasi AIS Legacy. Rilis terbaru adalah **`v0.1.0-uat.1`**, sebuah prerelease teknis untuk UAT
lokal/clone—belum merupakan persetujuan deployment produksi.

| Status | Nilai |
|---|---|
| Versi | `0.1.0-uat.1` |
| Runtime | Java 25, Spring Boot 4.1.1, PostgreSQL 16 |
| Arsitektur | Modular monolith + strangler/coexistence |
| UI | Thymeleaf, htmx, Tabler self-hosted, responsif |
| Database produksi Legacy | Tetap pemilik write sampai route disetujui |
| Bukti teknis | Fase 0–7 lulus UAT clone; Fase 8 tetap gated |

Catatan rilis lengkap tersedia di
[`docs/releases/v0.1.0-uat.1.md`](docs/releases/v0.1.0-uat.1.md), sedangkan riwayat ringkas ada di
[`CHANGELOG.md`](CHANGELOG.md).

## Prinsip keamanan terpenting

- Jangan arahkan migration, DDL, fixture, atau pengujian write ke database sumber `ais` maupun
  `streaming_ais`.
- Flyway hanya memiliki control-plane `ais_next_control`.
- Database tenant produksi harus memakai akun read-only sampai route dan aggregate memperoleh
  ownership `NEXT_WRITE` yang sah.
- Kredensial, signing key, token handoff, dan session cookie tidak boleh disimpan di repository.
- Login tetap dilakukan di AIS Legacy. AIS Next menerima identitas melalui signed one-time
  handoff sehingga password tidak diduplikasi.

## Quick start

Prasyarat: JDK 25, PostgreSQL 16 atau kompatibel, serta database control-plane terpisah.

```powershell
$env:AIS_CONTROL_DB_USERNAME = '<control-user>'
$env:AIS_CONTROL_DB_PASSWORD = '<control-password>'
$env:AIS_LOCAL_DB_USERNAME = '<legacy-read-user>'
$env:AIS_LOCAL_DB_PASSWORD = '<legacy-read-password>'
$env:AIS_HANDOFF_SIGNING_KEY = '<minimum-32-byte-random-secret>'

.\mvnw.cmd verify
java -jar .\apps\ais-next-web\target\ais-next-web-0.1.0-uat.1.jar
```

Buka [http://localhost:8081/](http://localhost:8081/) untuk landing page dan
[http://localhost:8081/actuator/health](http://localhost:8081/actuator/health) untuk health probe.
Dashboard terautentikasi hanya dapat dibuka setelah AIS Legacy menerbitkan token handoff yang sah.

Untuk pengujian operator pada localhost, gunakan database clone dan ikuti
[`docs/runbooks/local-smoke-test.md`](docs/runbooks/local-smoke-test.md). Penggunaan akun database
berprivilege tinggi hanya dibenarkan untuk pembuktian lokal yang terkontrol, bukan konfigurasi
produksi.

## Konfigurasi runtime

| Environment variable | Default | Kegunaan |
|---|---|---|
| `AIS_HTTP_PORT` | `8081` | Port HTTP aplikasi |
| `AIS_SECURE_COOKIE` | `false` | Wajib `true` di HTTPS |
| `AIS_TEMPLATE_CACHE` | `true` | Cache template Thymeleaf |
| `AIS_LEGACY_LOGIN_URL` | `http://localhost:18080/ais/login` | Tujuan login resmi Legacy |
| `AIS_CONTROL_DB_URL` | `jdbc:postgresql://localhost:5432/ais_next_control` | Control-plane Flyway dan routing |
| `AIS_CONTROL_DB_USERNAME` | kosong | User control-plane |
| `AIS_CONTROL_DB_PASSWORD` | kosong | Password control-plane |
| `AIS_LOCAL_CORE_DB_URL` | `jdbc:postgresql://localhost:5432/ais` | Sumber CORE lokal |
| `AIS_LOCAL_FILE_DB_URL` | `jdbc:postgresql://localhost:5432/streaming_ais` | Sumber FILE lokal |
| `AIS_LOCAL_DB_USERNAME` | kosong | User tenant; read-only di produksi |
| `AIS_LOCAL_DB_PASSWORD` | kosong | Password tenant |
| `AIS_HANDOFF_SIGNING_KEY` | kosong | Secret minimum 32 byte |
| `AIS_MAX_CACHED_POOLS` | `32` | Batas pool tenant dalam cache |
| `AIS_POOL_IDLE_TTL` | `10m` | Masa idle pool tenant |

Gunakan secret manager pada target environment. Jangan menambahkan nilai rahasia ke README,
`application.yml`, command history yang dibagikan, atau artefak rilis.

## Arsitektur

```text
trusted host + signed handoff
             |
       AIS Next web
             |
  route ownership + RBAC
             |
  TenantDataSourceRegistry
      |                 |
 CORE descriptor     FILE descriptor
      |                 |
 legacy schema      streaming schema

ais_next_control (RW, Flyway-owned)
  tenant, domain, route ownership, nonce, audit, outbox, file saga
```

Kode dibagi menjadi empat area:

| Area | Tanggung jawab |
|---|---|
| `platform/` | kernel, tenant routing, security, API, audit, observability, dan file boundary |
| `legacy/` | adapter identity, RBAC, API, file, dan kontrak schema Legacy |
| `modules/` | identity, organization, attendance, academic, supporting ERP, finance, integration |
| `apps/ais-next-web/` | executable Spring Boot, configuration, template, dan aset UI |

Spring Modulith dan ArchUnit menjaga batas modul. Seluruh class, constructor, dan method Java
harus memiliki JavaDoc; aturan ini diterapkan oleh `JavadocCoverageTest`.

## Kapabilitas yang tersedia

| Domain | UI | API | Ownership normal `local` |
|---|---|---|---|
| Identity/RBAC | `/dashboard`, `/profile`, `/search`, `/roles` | `/api/v1/roles` | Next read-only |
| Organization | `/school-types` | `/api/v1/school-types` | Legacy; Next hanya UAT clone |
| Attendance | `/attendance/daily` | `/api/v1/attendance/daily` | Legacy; Next hanya UAT clone |
| Academic | `/academic/students` | `/api/v1/academic/students` | Legacy; Next hanya UAT clone |
| Library | `/supporting/library/item-types` | `/api/v1/supporting/library/item-types` | Legacy; Next hanya UAT clone |
| Finance reference | `/finance/account-groups` | `/api/v1/finance/account-groups` | Legacy; Next hanya UAT clone |

OpenAPI tersedia di `/api/openapi.json` dan Swagger UI di `/api/docs`. Endpoint observability yang
diizinkan berada di bawah `/actuator`.

## UI/UX

UI menggunakan design system AIS yang responsif di atas aset Tabler self-hosted. Implementasi
mencakup landing page, handoff yang aman, dashboard berbasis peran, navigasi aktif dua tingkat,
menu mobile, pencarian, form/filter, tabel, status, focus state, reduced motion, dan skip link.

Kontrak Playwright memverifikasi desktop dan Pixel 7, termasuk WCAG 2.0 A/AA, 2.1 AA, dan 2.2 AA.
Lihat [`docs/baseline/ui-ux-parity-audit.md`](docs/baseline/ui-ux-parity-audit.md) dan
[`docs/evidence/ui-ux-shell-uat-2026-09-04.md`](docs/evidence/ui-ux-shell-uat-2026-09-04.md).

## Build dan pengujian

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify

Set-Location .\e2e
npm ci
npx playwright test
```

`verify` menjalankan policy dependency, unit/integration tests yang tersedia, JavaDoc coverage,
Spring Modulith checks, packaging executable JAR, dan CycloneDX SBOM. Testcontainers membutuhkan
Docker; jika Docker tidak tersedia, migration test tersebut di-skip dan harus diganti bukti
PostgreSQL langsung pada lingkungan UAT yang terisolasi.

Playwright suite tertentu memakai feature flag environment agar tidak pernah dijalankan tanpa
sengaja terhadap database sumber. Baca runbook fase terkait sebelum mengaktifkannya.

## Dokumentasi utama

- [`docs/migration-roadmap.md`](docs/migration-roadmap.md) — fase, status, dan exit gate.
- [`docs/adr/`](docs/adr/) — keputusan arsitektur.
- [`docs/runbooks/`](docs/runbooks/) — smoke test, UAT, rollback, dan retirement.
- [`docs/baseline/`](docs/baseline/) — kontrak hasil audit Legacy.
- [`docs/evidence/`](docs/evidence/) — bukti build, UAT, recovery, dan source invariance.
- [`docs/releases/`](docs/releases/) — isi, instalasi, checksum, rollback, dan batas setiap rilis.

## Disiplin perubahan dan rilis

Setiap perubahan source atau dokumentasi harus:

1. mempertahankan perubahan pengguna lain yang tidak terkait;
2. menambah/memperbarui test serta JavaDoc yang relevan;
3. memperbarui README dan changelog jika perilaku, konfigurasi, atau status rilis berubah;
4. lulus verifikasi sesuai risiko;
5. dibuat sebagai commit yang terfokus dan didorong ke remote; dan
6. untuk kandidat rilis, memakai versi non-SNAPSHOT, annotated tag, release notes, JAR, SBOM, dan
   checksum SHA-256.

Prerelease UAT tidak boleh dipromosikan menjadi production release hanya karena test lokal lulus.
Persetujuan domain owner, security/privacy/reconciliation, operasi, performa target, dan rollback
tetap harus dicatat per route.

## Scope dan status migrasi

Fase 0–2 dan kandidat teknis Fase 3–7 tersedia. Fase 8 sudah diaudit, tetapi tidak ada route Legacy
yang boleh dihentikan karena bukti traffic produksi, consumer disposition, retention/restore,
approval, dan expiry rollback window belum tersedia. Detail keputusan ada di
[`docs/evidence/phase-8-retirement-readiness-2026-09-04.md`](docs/evidence/phase-8-retirement-readiness-2026-09-04.md).
