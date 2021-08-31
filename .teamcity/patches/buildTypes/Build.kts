package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Build'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Build")) {
    params {
        add {
            param("env.SDN_NEO4J_URL", "%dep.CloudRoot_Neo4jCloud_Neo4jCloudSetupIntegrationTest.CONNECTIONURL_SECURE%")
        }
    }

    triggers {
        val trigger1 = find<VcsTrigger> {
            vcs {
            }
        }
        trigger1.apply {
            enabled = false
        }
        add {
            finishBuildTrigger {
                buildType = "CloudRoot_Neo4jCloud_Neo4jCloudSetupTestEnvironment"
            }
        }
    }
}
