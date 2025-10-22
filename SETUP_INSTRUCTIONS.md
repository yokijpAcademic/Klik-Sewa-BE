# 🚀 Setup Instructions - Klik Sewa Backend

## Prerequisites

1. **JDK 21** - Download dari [Oracle](https://www.oracle.com/java/technologies/downloads/) atau [OpenJDK](https://adoptium.net/)
2. **Docker Desktop** - Download dari [Docker](https://www.docker.com/products/docker-desktop)
3. **IntelliJ IDEA** (Recommended) atau text editor lain
4. **Postman** atau **Insomnia** untuk testing API

---

## Step-by-Step Setup

### 1. Clone Project

```bash
git clone <repository-url>
cd klik-sewa-BE
```

### 2. Setup Environment Variables

Copy file `.env.example` menjadi `.env`:

```bash
cp .env.example .env
```

Edit file `.env` dan sesuaikan:

```env
# Database - Biarkan default jika pakai Docker
MONGODB_URI=mongodb://localhost:27017/kliksewa_db

# Redis - Biarkan default jika pakai Docker
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT - GANTI dengan secret yang kuat!
JWT_SECRET=ganti-dengan-random-string-yang-panjang-dan-kompleks
JWT_EXPIRATION_IN_MINUTES=10080

# Email - Daftar di Brevo terlebih dahulu
BREVO_API_KEY=your-brevo-api-key
SENDER_EMAIL=noreply@yourdomain.com
SENDER_NAME=Klik Sewa

# Frontend URL
FRONTEND_URL=http://localhost:3000
```

### 3. Start MongoDB & Redis dengan Docker

```bash
# Start semua service
docker-compose up -d

# Cek apakah sudah jalan
docker-compose ps
```

Anda harus lihat output seperti:
```
NAME                      STATUS
kliksewa-mongodb          Up
kliksewa-redis            Up
kliksewa-mongo-express    Up
kliksewa-redis-commander  Up
```

### 4. Verifikasi Database & Redis

**MongoDB:**
- Buka browser: http://localhost:8081
- Login dengan:
    - Username: `admin`
    - Password: `admin123`
- Anda harus lihat database `kliksewa_db` (masih kosong)

**Redis:**
- Buka browser: http://localhost:8082
- Anda harus lihat Redis Commander interface

### 5. Build & Run Aplikasi

**Menggunakan Gradle:**

```bash
# Build project
./gradlew build

# Run aplikasi
./gradlew run
```

**Menggunakan IntelliJ IDEA:**
1. Open project di IntelliJ
2. Wait sampai Gradle sync selesai
3. Buka `src/main/kotlin/Application.kt`
4. Klik tombol ▶️ Run di samping `fun main()`

### 6. Verifikasi Aplikasi Berjalan

Jika sukses, Anda akan lihat log:

```
Application started in 0.303 seconds.
Responding at http://0.0.0.0:8080
```

Test dengan curl:

```bash
curl http://localhost:8080
```

---

## Struktur Project Setelah Setup

```
klik-sewa-BE/
├── src/main/kotlin/
│   ├── config/              ✅ Database, Redis, Email config
│   ├── di/                  ✅ Koin dependency injection
│   ├── features/
│   │   ├── auth/           ⏳ Implementasi auth (next step)
│   │   ├── listing/        ⏳ Implementasi listing (next step)
│   │   ├── category/       ⏳ Implementasi category (next step)
│   │   └── admin/          ⏳ Implementasi admin (next step)
│   ├── plugins/            ✅ CORS, Auth, Serialization, etc
│   └── shared/
│       ├── models/         ✅ User, Listing, Category models
│       ├── utils/          ✅ Hashing, JWT, Email utils
│       └── dtos/           ✅ Common response models
├── .env                    ✅ Environment variables
├── docker-compose.yml      ✅ Docker configuration
└── build.gradle.kts        ✅ Dependencies
```

**Legend:**
- ✅ = Sudah selesai & siap dipakai
- ⏳ = Belum diimplementasi (next step)

---

## Next Steps: Implementasi Features

Sekarang Anda siap untuk implementasi fitur-fitur:

### 1. Auth Module
- Register user
- Login
- Email verification
- JWT authentication
- Get user profile
- Update profile

### 2. Listing Module
- Create listing
- Get all listings (dengan filter & pagination)
- Get listing detail
- Update listing
- Delete listing
- Upload images

### 3. Category Module
- Get all categories
- Create category (admin only)
- Update category (admin only)
- Delete category (admin only)

### 4. Admin Module
- Dashboard statistics
- Approve/reject listings
- Manage users
- View reports

---

## Testing API

Setelah implementasi auth, Anda bisa test dengan Postman:

### Register User
```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!",
  "fullName": "John Doe",
  "phoneNumber": "081234567890"
}
```

### Login
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!"
}
```

Response akan berisi JWT token yang digunakan untuk request selanjutnya.

---

## Troubleshooting

### MongoDB Connection Error

**Error:** `Connection refused: connect`

**Solusi:**
1. Pastikan Docker container jalan: `docker-compose ps`
2. Restart container: `docker-compose restart mongodb`
3. Cek logs: `docker-compose logs mongodb`

### Redis Connection Error

**Error:** `Could not connect to Redis`

**Solusi:**
1. Pastikan Redis container jalan: `docker-compose ps`
2. Restart container: `docker-compose restart redis`
3. Test koneksi: `docker exec -it kliksewa-redis redis-cli ping` (harus return `PONG`)

### Port Already in Use

**Error:** `Address already in use: bind`

**Solusi:**

**Windows:**
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Mac/Linux:**
```bash
lsof -i :8080
kill -9 <PID>
```

### Build Failed

**Error:** Dependencies tidak terdownload

**Solusi:**
```bash
# Clean build
./gradlew clean

# Download dependencies
./gradlew build --refresh-dependencies
```

### Environment Variables Not Loaded

**Solusi:**
1. Pastikan file `.env` ada di root project
2. Restart aplikasi
3. Cek apakah `java-dotenv` dependency ada di `build.gradle.kts`

---

## Useful Commands

### Docker

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose stop

# View logs
docker-compose logs -f

# Restart specific service
docker-compose restart mongodb

# Clean up (HAPUS SEMUA DATA!)
docker-compose down -v
```

### Gradle

```bash
# Clean build
./gradlew clean

# Build
./gradlew build

# Run
./gradlew run

# Run tests
./gradlew test

# Build fat JAR
./gradlew buildFatJar
```

### MongoDB Commands (Inside Container)

```bash
# Masuk ke MongoDB shell
docker exec -it kliksewa-mongodb mongosh

# Di dalam mongosh:
show dbs
use kliksewa_db
show collections
db.users.find()
```

### Redis Commands (Inside Container)

```bash
# Masuk ke Redis CLI
docker exec -it kliksewa-redis redis-cli

# Di dalam redis-cli:
PING
KEYS *
GET key_name
```

---

## Production Deployment (Future)

Untuk deploy ke production, Anda perlu:

1. **Setup Environment:**
    - Ganti `JWT_SECRET` dengan secret yang kuat
    - Setup MongoDB Atlas atau MongoDB server
    - Setup Redis Cloud atau Redis server
    - Ganti `FRONTEND_URL` dengan domain frontend production

2. **Build Fat JAR:**
   ```bash
   ./gradlew buildFatJar
   ```

3. **Deploy:**
    - Upload JAR ke server
    - Setup systemd service atau Docker container
    - Configure reverse proxy (Nginx)
    - Setup SSL/TLS certificate

---

## Support

Jika ada pertanyaan atau error:
1. Cek logs aplikasi
2. Cek logs Docker: `docker-compose logs`
3. Baca dokumentasi di `DOCKER_TUTORIAL.md`
4. Search error message di Google/Stack Overflow

Happy Coding! 🎉