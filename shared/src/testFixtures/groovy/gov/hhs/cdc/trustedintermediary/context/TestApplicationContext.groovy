package gov.hhs.cdc.trustedintermediary.context

import gov.hhs.cdc.trustedintermediary.external.slf4j.LocalLogger
import gov.hhs.cdc.trustedintermediary.wrappers.Logger

/**
 * This test class resets the implementation registration in the ApplicationContext so different test cases can start on a clean slate.
 */
class TestApplicationContext extends ApplicationContext {

    def static init() {
        //initialize some default implementations that we want by default across nearly all tests
        register(Logger, LocalLogger.getInstance())
    }

    def static reset() {
        OBJECT_MAP.clear()
        IMPLEMENTATIONS.clear()
        TEST_ENV_VARS.clear()
    }

    def static injectRegisteredImplementations() {
        injectRegisteredImplementations(true)
    }

    def static addEnvironmentVariable(String key, String value) {
        TEST_ENV_VARS.put(key, value)
    }
}
