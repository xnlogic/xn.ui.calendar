(defproject xn.ui.calendar "1.0.0-SNAPSHOT"
  :description "Calendar picker with date-range and time abilities"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2356"]
                 ; xn.library pulls in current versions of om and cljs-time, etc.
                 [xn.library.cljs "0.1.0"]
                 [figwheel "0.1.4-SNAPSHOT"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-figwheel "0.1.4-SNAPSHOT"]]

  :source-paths ["src"]

  :cljsbuild
  {:builds [{:id "dev"
             :source-paths ["src" "dev"]
             :compiler {:output-to "resources/public/js/compiled/xn/ui/calendar.js"
                        :output-dir "resources/public/js/compiled/out"
                        :optimizations :none
                        :source-map true}}
            {:id "demo"
             :source-paths ["src" "dev"]
             :compiler {:output-to "resources/demo/xn.ui.calendar.min.js"
                        :source-map "resources/demo/xn.ui.calendar.map"
                        :output-dir "resources/demo/xn.ui.calendar.src"
                        :optimizations :advanced
                        :preamble ["react/react.min.js"]
                        :externs ["react/externs/react.js"]}}]}
  :figwheel
  {:http-server-root "public" ;; default and assumes "resources"
   :server-port 3450 ;; default + 1
   :css-dirs ["public/resources/css"]})
