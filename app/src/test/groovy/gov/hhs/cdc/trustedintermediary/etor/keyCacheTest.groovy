package gov.hhs.cdc.trustedintermediary.etor

import spock.lang.Specification

class keyCacheTest extends Specification {

    def "keyCache works"() {
        given:
        def cache = KeyCache.getInstance()
        def expected = "fake_key"
        def key = "report_stream"
        when:
        cache.put(key, expected)
        def actual = cache.get(key)
        then:
        actual == expected
    }

    def "keyCache synchronization works"() {
        given:
        def cache = KeyCache.getInstance()
        def threadsNum = 5
        def iterations = 25

        when:
        List<Thread> threads = []
        (1..threadsNum).each { threadId ->
            threads.add(new Thread({
                for (int i = 0; i < iterations; i++) {
                    cache.put("Thread-" + threadId, "${i}")
                }
            }))
        }

        threads*.start()
        threads*.join()
        def keysNum = cache.getProperties().get("keys").collect().toArray().size()
        def keys = cache.getProperties().get("keys").collect().collectEntries()
        println("keys: " + keys)
        println(keysNum)
        println(cache.getProperties())

        then:
        keysNum == threadsNum
        keys.get("Thread-1") == "24"
        keys.get("Thread-2") == "24"
        keys.get("Thread-3") == "24"
        keys.get("Thread-4") == "24"
        keys.get("Thread-5") == "24"
        keys.size() == 5 // one entry per thread
        keys.values().toSet().size() == 1 // all entries have same value, threads had to wait on the lock

    }
}
