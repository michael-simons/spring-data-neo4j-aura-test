import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.add
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

version = "2020.2"

object SDNRepo : GitVcsRoot({
	id = AbsoluteId("spring_data_neo4j")
	name = "spring-data-neo4j"
	url = "https://github.com/spring-projects/spring-data-neo4j.git"
	branch = "main"
})

project {
	buildType(Build)
	vcsRoot(SDNRepo)
	params {
		add {
			param("env.SDN_NEO4J_URL", "%dep.CloudRoot_Neo4jCloud_Neo4jCloudSetupIntegrationTest.CONNECTIONURL_SECURE%")
		}
		add {
			param("env.SDN_NEO4J_PASSWORD", "%dep.CloudRoot_Neo4jCloud_Neo4jCloudSetupIntegrationTest.PASSWORD%")
		}
	}
}

object Build : BuildType({
	name = "Build"

	vcs {
		root(DslContext.settingsRoot.id!!, "-:.teamcity", "+:src => bin")
		root(SDNRepo, "+:. => work/spring-data-neo4j")
		cleanCheckout = true
	}

	dependencies {
		add(AbsoluteId("CloudRoot_Neo4jCloud_Neo4jCloudSetupIntegrationTest")) {
			snapshot {
				reuseBuilds = ReuseBuilds.NO
				onDependencyFailure = FailureAction.FAIL_TO_START
				synchronizeRevisions = false
			}
		}
	}

	steps {
		exec {
			name = "Run SDN cluster tests."
			path = "./bin/runClusterTests.sh"
			dockerImage = "openjdk:11"
			dockerRunParameters = "--volume /var/run/docker.sock:/var/run/docker.sock"
		}
	}

	requirements {
		add {
			startsWith("cloud.amazon.agent-name-prefix", "linux")
		}
	}

	triggers {
		vcs {
			enabled = false
		}
		finishBuildTrigger {
			buildType = "CloudRoot_Neo4jCloud_Neo4jCloudSetupTestEnvironment"
		}
	}
})
