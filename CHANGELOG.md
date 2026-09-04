# Changelog

Perubahan penting AIS Next dicatat di file ini. Catatan teknis, prosedur instalasi, rollback, dan
gate lengkap disimpan terpisah agar changelog tetap ringkas.

## [0.1.0-uat.1] — 2026-09-04

Prerelease UAT pertama untuk foundation AIS Next dan kandidat migrasi Fase 0–7.

### Added

- Modular monolith Java 25, tenant routing, route ownership, signed one-time handoff, RBAC, audit,
  observability, OpenAPI, control-plane Flyway, dan file boundary.
- Slice Identity/RBAC, Jenis Sekolah, Kehadiran Harian, Direktori Siswa, Jenis Item Perpustakaan,
  serta Grup Akun Keuangan.
- UI/UX responsif self-hosted untuk landing, dashboard, navigasi, pencarian, form, dan tabel.
- UAT clone, source-invariance, route rollback, outage recovery, dan browser accessibility suites.
- Gate retirement Fase 8 yang mempertahankan seluruh route serta data Legacy.

### Changed

- Seluruh project Maven memakai versi prerelease non-SNAPSHOT `0.1.0-uat.1`.
- README utama kini memuat quick start, konfigurasi, modul, route, keamanan, testing, dokumentasi,
  dan disiplin release.

### Security

- Database sumber tetap bebas migration/DDL/fixture AIS Next.
- Password tidak diduplikasi; sesi Next hanya berasal dari handoff bertanda tangan dan sekali pakai.
- Data student, library, dan finance menggunakan proyeksi minimum serta exact role authorization.

### Known gates

- Rilis ini bukan approval produksi.
- Domain-owner, security/privacy/reconciliation, operations, target performance, retention, dan
  rollback-window evidence masih diperlukan per route.

Lihat [catatan rilis lengkap](docs/releases/v0.1.0-uat.1.md).
