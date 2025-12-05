# 프레임로그 백엔드

> 프레임 로그는 사진에 관심을 갖고 있는 사람들을 위한 커뮤니티입니다.  
> 각자의 작업물을 홍보하고 소통 가능하도록 하였습니다.


## 0. 기술적으로 고민한 지점과 해결
- [제대로 된 테이블 설계하기](https://tidal-tub-cac.notion.site/293e569146a6805d9c99c8594974843a?source=copy_link)
- [조회수에 Eventual Consistency 적용하기](https://tidal-tub-cac.notion.site/Eventual-Consistency-2a0e569146a680cabef5e7850860fde3?source=copy_link)
- [유효한 테스트 코드가 가져야 할 덕목](https://tidal-tub-cac.notion.site/2bfe569146a680adb005d0989ccff18d?source=copy_link)
- [인플루언서의 회원 탈퇴에는 6분이 걸린다..? (미완성)](https://tidal-tub-cac.notion.site/2b9e569146a6806bb8b9cc725e1b1b62?source=copy_link)


## 1. 프로젝트 개요
- **목적**: 요구사항 정의서를 기반으로 커뮤니티 플랫폼 설계 및 구현
- **기간**: 2025.10 ~ 2025.12 (개인 진행)
- **핵심 가치**
  1. **시스템 안정성**: Eventual Consistency 를 도입한 좋아요 처리
  2. **품질 지향**: 단위·통합·성능·뮤테이션 테스트를 통한 회귀 방지
  3. **올바른 테이블 설계**: 정규화를 기반으로 올바른 테이블 설계
  
## 2. 사용 기술 & 도구
| 구분 | 스택                                                         |
| --- |------------------------------------------------------------|
| 언어/프레임워크 | Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security |
| 데이터 | MySQL 8 (운영), H2 (테스트), P6Spy                              |
| 인증/보안 | JWT, Custom Filter, Method Security                        |
| 문서화 | springdoc-openapi Swagger UI                               |
| 테스트/품질 | JUnit5, Jacoco, Mockito, PITest                            |
| 기타 | Spring Scheduling, AOP ( MDC 기반 로깅 )                       |

## 3. 아키텍처 및 모듈 구성
```
com.community
├─ domain
│  ├─ auth      : JWT 발급/검증, 로그인·토큰 재발급 플로우
│  ├─ user      : 회원가입, 프로필, 비밀번호, 탈퇴
│  ├─ board     : 게시글/댓글/좋아요/조회수 이벤트
│  └─ file      : 인메모리 파일 저장, 업로드/다운로드
└─ global       : 공통 응답, 예외, 시큐리티, AOP, Validator
```
- **레이어드 아키텍처**: Controller -> Service -> Repository -> Entity 구조를 유지하면서 DTO/Response 계층을 세분화해 응답 계약을 명확히 정의했습니다.
- **관심사 분리**: Custom JWT filter, PermissionEvaluator 등을 global 패키지에 두어 도메인 로직과 보안 로직을 분리했습니다.

## 4. 주요 기능 하이라이트
1. **JWT 복합 인증 플로우**  
   - `TokenAuthResponseMaker`로 AccessToken + HttpOnly RefreshToken을 동시 발급  
   - Refresh 요청 시 쿠키 기반 재발급, `AuthUserArgumentResolver`로 컨트롤러에 인증 사용자 주입  
2. **게시판 도메인**  
   - 멀티파트 게시글 작성/수정, 이미지 경로 관리  
   - 좋아요 토글 API, 내가 쓴 글/좋아요한 글/전체 글 페이지네이션  
3. **조회수 비동기 처리**  
   - `PostViewEventService`가 조회 이벤트 테이블에 적재 후 `@Scheduled` 를 통해 배치로 이벤트를 처리 
   - 빠른 요청 응답 + DB 부하 완화
4. **파일 업로드**  
   - `LocalFileStorageService`로 인메모리 저장 → `/files/{fileId}` 다운로드/삭제  
   - 추후 S3 등으로 구현체 교체 가능한 인터페이스 분리  
5. **공통 응답 & 로깅**  
   - `ApiResponse`로 모든 컨트롤러 응답을 통합  
   - `ControllerExecutionTimeAspect` + MDC RequestId로 레이어별 수행 시간 로그화


## 5. 테스트 & 품질 지표

Service 계층 커버리지 목표 80 % , Repository 계층 커버리지 60 % 설정 및 달성  
PITest 를 통해서 뮤테이션을 기반으로 테스트 코드의 로직 방어력 검증

---

