# DB 스터디

스터디 모임 관리 서비스

# 실행 방법


## IDE에서 실행 방법

IDE에서 프로젝트로 로딩한 다음 메이븐으로 컴파일 빌드를 하고 App.java 클래스를 실행합니다.

### 메이븐으로 컴파일 빌드 하는 방법

메이븐이 설치되어 있지 않은 경우 메이븐 랩퍼(mvnw 또는 mvnw.cmd(윈도))를 사용해서 빌드하세요.

```
mvnw compile
```

메이븐으로 컴파일을 해야 프론트엔드 라이브러리를 받아오며 QueryDSL 관련 코드를 생성합니다.

## 콘솔에서 실행 방법

JAR 패키징을 한 뒤 java -jar로 실행합니다.

```
mvnw clean compile package
```

```
java -jar target/*.jar
```

# DB 설정
PostgreSQL 설치 후, psql로 접속해서 아래 명령어 사용하여 DB와 USER 생성하고 권한 설정.

```sql
CREATE DATABASE testdb;
CREATE USER testuser WITH ENCRYPTED PASSWORD 'testpass';
GRANT ALL PRIVILEGES ON DATABASE testdb TO testuser;
```

이 프로젝트는 인프런에 있는 백기선 님의 강의를 수강하며 작성한 프로젝트입니다.
코드에 대한 모든 저작권은 백기선 님께 있음을 명시합니다.

출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-JPA-%EC%9B%B9%EC%95%B1/dashboard
