# 🐳 Tutorial Docker untuk Pemula - Klik Sewa Project

## Apa itu Docker?

Docker adalah platform yang memungkinkan Anda menjalankan aplikasi dalam **container**. Container seperti "kotak virtual" yang berisi semua yang dibutuhkan aplikasi untuk berjalan (database, redis, dll) tanpa harus install manual di komputer Anda.

### Analogi Sederhana:
Bayangkan Anda punya resep masakan. Docker seperti:
- **Image** = Resep masakan
- **Container** = Masakan yang sudah jadi (hasil dari resep)
- **Docker Compose** = Buku resep lengkap dengan cara masak beberapa menu sekaligus

---

## Instalasi Docker

### Windows & Mac:
1. Download **Docker Desktop** dari: https://www.docker.com/products/docker-desktop
2. Install seperti aplikasi biasa
3. Jalankan Docker Desktop
4. Pastikan Docker sudah jalan (ada icon whale di system tray)

### Linux (Ubuntu):
```bash
# Update package
sudo apt update

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt install docker-compose

# Tambah user ke docker group (agar tidak perlu sudo)
sudo usermod -aG docker $USER
newgrp docker
```

Cek instalasi:
```bash
docker --version
docker-compose --version
```

---

## Struktur docker-compose.yml Kita

File `docker-compose.yml` berisi 4 service:

1. **mongodb** - Database utama
2. **redis** - Cache & session storage
3. **mongo-express** - GUI untuk melihat isi MongoDB (optional)
4. **redis-commander** - GUI untuk melihat isi Redis (optional)

---

## Cara Menggunakan Docker untuk Project Ini

### 1. Setup Awal

Pastikan file `docker-compose.yml` sudah ada di root project Anda.

### 2. Jalankan Semua Service

```bash
# Jalankan di terminal/command prompt di folder project
docker-compose up -d
```

**Penjelasan:**
- `docker-compose up` = Jalankan semua service
- `-d` = Detached mode (berjalan di background)

**Output yang Anda lihat:**
```
Creating network "kliksewa-network" ...
Creating volume "kliksewa_mongodb_data" ...
Creating volume "kliksewa_redis_data" ...
Creating kliksewa-mongodb ...
Creating kliksewa-redis ...
Creating kliksewa-mongo-express ...
Creating kliksewa-redis-commander ...
```

### 3. Cek Status Container

```bash
docker-compose ps
```

Output:
```
NAME                      STATUS    PORTS
kliksewa-mongodb          Up        0.0.0.0:27017->27017/tcp
kliksewa-redis            Up        0.0.0.0:6379->6379/tcp
kliksewa-mongo-express    Up        0.0.0.0:8081->8081/tcp
kliksewa-redis-commander  Up        0.0.0.0:8082->8081/tcp
```

### 4. Akses GUI (Optional)

**MongoDB Express:**
- URL: http://localhost:8081
- Username: `admin`
- Password: `admin123`
- Di sini Anda bisa lihat database, collections, dan data

**Redis Commander:**
- URL: http://localhost:8082
- Di sini Anda bisa lihat keys yang tersimpan di Redis

### 5. Lihat Logs (Debugging)

```bash
# Lihat semua logs
docker-compose logs

# Lihat logs MongoDB saja
docker-compose logs mongodb

# Lihat logs Redis saja
docker-compose logs redis

# Follow logs (real-time)
docker-compose logs -f
```

### 6. Stop Semua Service

```bash
# Stop tapi data tetap ada
docker-compose stop

# Stop dan hapus container (data tetap ada)
docker-compose down

# Stop dan hapus SEMUA termasuk data (HATI-HATI!)
docker-compose down -v
```

### 7. Restart Service

```bash
# Restart semua
docker-compose restart

# Restart MongoDB saja
docker-compose restart mongodb

# Restart Redis saja
docker-compose restart redis
```

---

## Command Docker yang Sering Dipakai

### Manajemen Container

```bash
# Lihat semua container yang berjalan
docker ps

# Lihat semua container (termasuk yang stop)
docker ps -a

# Masuk ke dalam container (seperti SSH)
docker exec -it kliksewa-mongodb bash

# Stop container tertentu
docker stop kliksewa-mongodb

# Start container tertentu
docker start kliksewa-mongodb

# Hapus container
docker rm kliksewa-mongodb
```

### Manajemen Image

```bash
# Lihat semua image yang terdownload
docker images

# Hapus image
docker rmi mongo:7.0

# Pull image dari Docker Hub
docker pull redis:7-alpine
```

### Manajemen Volume (Data Storage)

```bash
# Lihat semua volume
docker volume ls

# Inspect volume
docker volume inspect kliksewa_mongodb_data

# Hapus volume (HATI-HATI! Data akan hilang)
docker volume rm kliksewa_mongodb_data
```

### Clean Up

```bash
# Hapus semua container yang stop
docker container prune

# Hapus semua image yang tidak dipakai
docker image prune

# Hapus semua volume yang tidak dipakai
docker volume prune

# Clean up total (HATI-HATI!)
docker system prune -a
```

---

## Troubleshooting

### Port Sudah Dipakai

**Error:**
```
Error: bind: address already in use
```

**Solusi:**

1. **Windows:**
   ```powershell
   # Cek apa yang pakai port 27017
   netstat -ano | findstr :27017
   
   # Kill process (ganti PID dengan nomor yang muncul)
   taskkill /PID 1234 /F
   ```

2. **Mac/Linux:**
   ```bash
   # Cek apa yang pakai port 27017
   lsof -i :27017
   
   # Kill process
   kill -9 PID
   ```

3. **Atau ubah port di docker-compose.yml:**
   ```yaml
   ports:
     - "27018:27017"  # Ganti 27017 jadi 27018
   ```
   Jangan lupa update `.env`:
   ```
   MONGODB_URI=mongodb://localhost:27018/kliksewa_db
   ```

### Container Terus Restart

```bash
# Lihat logs untuk cari error
docker-compose logs mongodb

# Biasanya karena:
# - Port conflict
# - Volume permission error
# - Out of memory
```

### Tidak Bisa Connect ke MongoDB/Redis

1. Pastikan container berjalan: `docker-compose ps`
2. Cek logs: `docker-compose logs`
3. Pastikan port tidak diblock firewall
4. Cek connection string di `.env` sudah benar

---

## Workflow Development Anda

### Sehari-hari:

1. **Pagi (Start Work):**
   ```bash
   docker-compose up -d
   # Atau kalau sudah pernah jalan, tinggal:
   docker-compose start
   ```

2. **Develop:**
    - Jalankan aplikasi Ktor Anda
    - MongoDB & Redis sudah running di background
    - Akses Mongo Express kalau mau lihat data

3. **Malam (End Work):**
   ```bash
   docker-compose stop
   # Data tetap aman!
   ```

### Testing Clean Database:

```bash
# Stop dan hapus semua (data juga)
docker-compose down -v

# Start fresh
docker-compose up -d
```

---

## Tips & Best Practices

### 1. Jangan Commit File Sensitive
Tambahkan ke `.gitignore`:
```
.env
docker-compose.override.yml
```

### 2. Backup Data Production

```bash
# Backup MongoDB
docker exec kliksewa-mongodb mongodump --out /backup
docker cp kliksewa-mongodb:/backup ./mongodb-backup

# Backup Redis
docker exec kliksewa-redis redis-cli SAVE
docker cp kliksewa-redis:/data/dump.rdb ./redis-backup
```

### 3. Environment-Specific Config

Buat `docker-compose.override.yml` untuk override local:
```yaml
version: '3.8'
services:
  mongodb:
    ports:
      - "27018:27017"  # Port berbeda
```

### 4. Monitor Resource Usage

```bash
# Lihat CPU & Memory usage
docker stats
```

---

## Kenapa Pakai Docker?

✅ **Konsisten**: Environment sama di semua komputer
✅ **Cepat Setup**: Install sekali, jalan di mana saja
✅ **Isolasi**: Tidak bentrok dengan software lain
✅ **Easy Cleanup**: Hapus container, sistem bersih kembali
✅ **Production-Ready**: Config yang sama bisa deploy ke server

---

## Dokumentasi Lengkap

- Docker: https://docs.docker.com
- Docker Compose: https://docs.docker.com/compose
- MongoDB Docker: https://hub.docker.com/_/mongo
- Redis Docker: https://hub.docker.com/_/redis

---

## Pertanyaan?

Kalau ada error atau bingung, cek:
1. `docker-compose logs` untuk lihat error message
2. `docker ps` untuk pastikan container jalan
3. Google error message yang muncul
4. Stack Overflow biasanya punya solusi!

Selamat coding! 🚀