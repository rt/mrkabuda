(defproject mrkabuda "0.1.0-SNAPSHOT"
  :description "mrkabuda 3d"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  ;; :dependencies [[org.clojure/clojure "1.10.1"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [unicode-math "0.2.0"]
                 [scad-clj "0.4.0"]]
  :main ^:skip-aot mrkabuda.core
  ;; :main mrkabuda.core
  :target-path "target/%s"
  ;; :profiles {:uberjar {:aot :all
  ;;                      :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  )
