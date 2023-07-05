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
    }

    def static injectRegisteredImplementations() {
        injectRegisteredImplementations(true)
    }
}
