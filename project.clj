(defproject counterpoint "0.1.0-SNAPSHOT"
  :description "Counterpoint generator"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [overtone "0.10.1"]]
  :main ^:skip-aot counterpoint.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
