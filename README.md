# GGUD

중간 지점 기반 약속 관리 플랫폼
캡스톤디자인 졸업 프로젝트

---

## 프로젝트 소개

여러 사용자의 위치를 기반으로 중간 지점을 추천하고,
약속 장소 탐색 / 실시간 위치 공유 / 정산 기능 등을 제공하는 앱.

Android, iOS, Server 파트 협업 프로젝트.

---

## 주요 기능

- 중간 지점 추천
- 카카오맵 기반 지도 표시
- 실시간 위치 공유
- 장소 추천
- 약속 생성 및 관리
- 정산 기능

---

## Android 구현 내용

- Jetpack Compose 기반 UI
- KakaoMap API 연동
- WebSocket(STOMP) 실시간 위치 공유
- Retrofit API 연동
- StateFlow 기반 상태 관리
- Navigation 구성
- 마커 및 경로 시각화

---

## 기술 스택

- Kotlin
- Jetpack Compose
- Coroutine
- StateFlow
- Retrofit2
- OkHttp
- WebSocket(STOMP)
- KakaoMap API

---

## 프로젝트 구조

```text
ui/
 ├── home
 ├── map
 ├── promise
 ├── recommendation
 ├── calculate
 └── notification

network/
data/
```

## 실행 방법

```bash
git clone https://github.com/2025-INU/GgUd-Android.git
```

- Android Studio에서 프로젝트 실행
- `local.properties` 설정 필요

```properties
KAKAO_REST_API_KEY=YOUR_KEY
```

- Emulator 또는 실제 기기에서 실행
- 위치 권한 허용 필요
