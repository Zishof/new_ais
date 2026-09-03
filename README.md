# AIS Next

AIS Next adalah fondasi modular-monolith Java 25 yang berjalan berdampingan dengan AIS legacy. Implementasi saat ini menyelesaikan foundation dan vertical slice baca-saja pertama: resolusi tenant dari trusted host, koneksi `CORE`/`FILE` yang lazy dan terbatas, signed one-time login handoff, daftar/detail grup pengguna dari tabel legacy, REST `/api/v1`, OpenAPI, serta UI Thymeleaf + htmx + Tabler yang seluruh asetnya self-hosted.

AIS legacy tetap menjadi pemilik write. Aplikasi ini tidak menjalankan migration atau DDL ke database tenant legacy. Flyway hanya diarahkan ke database control-plane terpisah, default `ais_next_control`.

## Arsitektur singkat

```text
trusted host / signed handoff
              |
        AIS Next web
              |
   TenantDataSourceRegistry
       |              |
   CORE (RO)       FILE (RO)
       \              /
        schema legacy

ais_next_control (RW, Flyway-owned)
  tenant, domain, route ownership, nonce, audit, outbox, file saga
```

Modul Maven berada di `platform/`, `legacy/`, `modules/`, dan executable application di `apps/ais-next-web`. Keputusan desain dan batasannya dicatat di `docs/adr/`.

## Prasyarat

- OpenJDK 25
- PostgreSQL 16 atau versi kompatibel
- Database control-plane kosong bernama `ais_next_control`
- Database legacy tersedia; untuk bootstrap lokal defaultnya `ais` (CORE) dan `streaming_ais` (FILE)

Maven lokal tidak dibutuhkan karena repository menyertakan Maven Wrapper.

## Konfigurasi lokal

Jangan simpan kredensial ke repository. Atur environment variable pada sesi PowerShell:

```powershell
$env:AIS_CONTROL_DB_USERNAME = '<control-user>'
$env:AIS_CONTROL_DB_PASSWORD = '<control-password>'
$env:AIS_LOCAL_DB_USERNAME = '<legacy-read-user>'
$env:AIS_LOCAL_DB_PASSWORD = '<legacy-read-password>'
$env:AIS_HANDOFF_SIGNING_KEY = '<minimum-32-byte-random-secret>'
```

Override yang umum: `AIS_CONTROL_DB_URL`, `AIS_LOCAL_CORE_DB_URL`, `AIS_LOCAL_FILE_DB_URL`, `AIS_HTTP_PORT`, dan `AIS_SECURE_COOKIE`. Akun database tenant produksi wajib read-only; `root` hanya dipakai untuk pembuktian localhost atas permintaan operator.

## Build dan test

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

`verify` juga menghasilkan CycloneDX SBOM. Untuk smoke test ke database localhost yang sudah disediakan, jalankan aplikasi lalu lakukan handoff satu-kali; runbook lengkap ada di `docs/runbooks/local-smoke-test.md`.

## Endpoint awal

- `GET /` — landing page publik
- `GET /auth/handoff?token=...` — konsumsi token login satu-kali
- `GET /dashboard` — dashboard awal
- `GET /roles` dan `GET /roles/{roleId}` — UI baca-saja
- `GET /api/v1/roles` dan `GET /api/v1/roles/{roleId}` — API baca-saja
- `GET /api/openapi.json` dan `/api/docs` — kontrak OpenAPI
- `GET /actuator/health` — health probe tanpa detail sensitif

## Status scope

Ini bukan klaim bahwa seluruh AIS telah dimigrasikan. Slice CRUD baru sengaja belum diaktifkan sampai ada clone database, pemilihan master data oleh pemilik bisnis, bukti audit/paritas, dan UAT. Roadmap dan exit criteria tersedia di `docs/migration-roadmap.md`.
