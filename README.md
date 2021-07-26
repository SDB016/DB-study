# DB 스터디

스터디 모임 관리 서비스

# 실행 방법


## IDE에서 실행 방법

IDE에서 프로젝트로 로딩한 다음 메이븐으로 컴파일 빌드를 하고 App.java 클래스를 실행합니다.

### 메이븐으로 컴파일 빌드 하는 방법

```
mvn compile
```

메이븐으로 컴파일을 해야 프론트엔드 라이브러리를 받아오며 QueryDSL 관련 코드를 생성합니다.

## 콘솔에서 실행 방법

JAR 패키징을 한 뒤 java -jar로 실행합니다.

```
mvn clean compile package
```

```
java -jar target/*.jar
```

이 프로젝트는 인프런에 있는 백기선 님의 강의를 수강하며 작성한 프로젝트입니다.
코드에 대한 모든 저작권은 백기선 님께 있음을 명시합니다.

출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-JPA-%EC%9B%B9%EC%95%B1/dashboard
