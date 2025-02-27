plugins {
  id("carbon.publishing-conventions")
}

description = "API for interfacing with the CarbonChat Minecraft mod/plugin"

dependencies {
  // Doesn't add any dependencies, only version constraints
  api(platform(libs.adventureBom))
  api(platform(libs.log4jBom))

  // Provided by platform
  compileOnlyApi(libs.adventureApi)
  compileOnlyApi(libs.adventureTextSerializerPlain)
  compileOnlyApi(libs.adventureTextSerializerLegacy)
  compileOnlyApi(libs.adventureTextSerializerGson) {
    exclude("com.google.code.gson")
  }
  compileOnlyApi(libs.minimessage)

  compileOnlyApi(libs.checkerQual)

  // Provided by Minecraft
  compileOnlyApi(libs.gson)
  compileOnlyApi(libs.log4jApi)
}
