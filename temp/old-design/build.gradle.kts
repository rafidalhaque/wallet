plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.design"
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.ui.core)

    implementation(projects.shared.domain)
}