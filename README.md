# Checkify
[![](https://jitpack.io/v/PartlySunnyDev/Checkify.svg)](https://jitpack.io/#PartlySunnyDev/Checkify)
![](https://img.shields.io/github/languages/top/PartlySunnyDev/Checkify)
![](https://img.shields.io/github/v/release/PartlySunnyDev/Checkify)
![](https://img.shields.io/github/stars/PartlySunnyDev/Checkify?style=social)

## Simple and easy predicate system
Easy way to add checking functionality to things like configuration files. Allow users to build complex logic using just strings!

### Features
- [x] Easy to use
- [x] Convert text into predicates in code

### Setup
1. Add the jitpack repository to your pom.xml
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url> <!-- Add this repository -->
    </repository>
</repositories>
```

2. Add the dependency to your pom.xml
```xml
<dependencies>
    <dependency>
        <groupId>com.github.PartlySunnyDev</groupId>
        <artifactId>Checkify</artifactId>
        <version>VERSION</version> <!-- Add this dependency -->
    </dependency>
</dependencies>
```

3. Shade the dependency into your plugin
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId> <!-- Add this plugin -->
            <version>3.2.4</version>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>me.partlysunny.checkify</pattern>
                        <shadedPattern>your.plugin.package.checkify</shadedPattern> <!-- Add this relocation -->
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>
```