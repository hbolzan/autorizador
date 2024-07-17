(ns autorizador.diplomat.db)

(def databases (atom {}))

(defn init-db!
  "Initialize a db with db-id key at databases atom"
  ([db-id] (init-db! db-id {}))
  ([db-id content]
   (swap! databases assoc db-id (atom content))))

(defn one-record
  [db-id id]
  (-> databases deref (get db-id) deref (get id)))

(defn update-record!
  "Generic update record function that sets the value of a key in db-id database
  - data-record must contain an :id key
  - default swap-nf is assoc"
  ([db-id data-record] (update-record! db-id data-record assoc))
  ([db-id data-record swap-fn]
   (swap! (get @databases db-id) swap-fn (-> data-record :id) data-record)))

(defn save-db!
  "Save database data into file"
  [db-id file-name]
  (spit file-name (-> databases deref (get db-id) deref)))

(defn load-db!
  "Load data from edn file into db"
  [db-id file-name]
  (let [content (slurp file-name)]
    ))
