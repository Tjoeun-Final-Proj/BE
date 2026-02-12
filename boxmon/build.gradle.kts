plugins {
    java
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.tjoeun"
version = "0.0.1-SNAPSHOT"
description = "boxmon"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    //Spring Boot starter
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation ("org.springframework.boot:spring-boot-starter-security")

    //test
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    //lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    //mysql
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.hibernate.orm:hibernate-spatial") // MySQL의 공간 데이터를 처리할 수 있도록 Hibernate Spatial 라이브러리
    
    //google map
    implementation("com.google.maps:google-maps-services:2.2.0") // Google Maps Services Java SDK(화물 상세정보 - 예상 도착 시간 연산·표시에 사용)
    
    //jwt
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    
    //암호화
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.security:spring-security-crypto")

    //외부 api 호출용(토스)
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("org.springframework.boot:spring-boot-starter-restclient")

    //swagger-ui 자동 생성
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
