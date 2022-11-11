package gov.hhs.cdc.trustedintermediary.context
/**
 * This test class resets the Object map from the ApplicationContext so different test cases can start on a clean slate
 */
class TestApplicationContext extends ApplicationContext{


    def static reset() {
        OBJECT_MAP.clear()
    }
}
