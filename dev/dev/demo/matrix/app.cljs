(ns dev.demo.matrix.app)

(do (require '[shadow.cljs.devtools.server :as shadow.server]
             '[shadow.cljs.devtools.api :as shadow])
    (shadow.server/start!)
    (shadow/watch :script)
    (shadow/nrepl-select :script))
