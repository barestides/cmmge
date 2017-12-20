(defproject cmmge "0.1.0-SNAPSHOT"
  :description "Some musical programming experiments"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [overtone "0.10.1"]
                 [roul "0.2.0"]]
  :main ^:skip-aot cmmge.core
  :jvm-opts ^:replace []
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
