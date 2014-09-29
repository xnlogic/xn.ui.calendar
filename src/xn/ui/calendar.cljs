(ns xn.ui.calendar
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as str]
            [cljs-time.core :as time]
            [cljs-time.local :as ltime]
            [cljs-time.format :as ftime]
            [xn.library.om :refer [swap-state!]]
            [xn.library.date-utils :refer [date+time at-hour at-minute on-or-before? should-be-local]]
            [xn.library.svg :as svg]
            [xn.library.element :refer [table]]
            [xn.library.popup :refer [popup position-popup]]))

(defn month-name [month]
  (str (ftime/months (dec (time/month month)))
       " "
       (time/year month)))

(defn date-list [month]
  (let [start-offset (time/days (time/day-of-week month))
        start-date (time/minus month start-offset)
        one-day (time/days 1)]
    (take 42
          (iterate #(time/plus % one-day) start-date))))

(defn date-button [on-click string]
  (dom/button
    #js {:className "btn btn-default selected-date-range-btn"
         :onClick on-click
         :ref "date-button"}
    (dom/div
      #js {:className "pull-left"}
      (dom/span
        #js {:className "glyphicon glyphicon-calendar"}))
    (dom/div
      #js {:className "pull-right"}
      (dom/span
        nil
        string
        " ")
      (dom/span
        #js {:className "caret"}))))

(def date-format (ftime/formatter "EEE, d MMM. yyyy"))
(def time-format (ftime/formatter "h:mm A"))
(def datetime-format (ftime/formatter "EEE, d MMM. yyyy, h:mm A"))

(declare calendar clock)

(defn datetime-component [cur owner {:keys [className key did-change will-change formatter]
                                     :or {will-change identity}}]
  (let [value (key cur)
        formatter (or formatter datetime-format)]
    (reify
      om/IRenderState
      (render-state [_ {:keys [month visible]}]
        (dom/div
          #js {:className "calendar-control"}
          (date-button
            #(swap-state! owner update-in [:visible] not)
            (if value
              (ftime/unparse-local formatter value)
              "no date selected"))
          (popup owner
                 (when visible
                   (dom/div
                     #js {:ref "popup"
                          :className "calendar-popup cal-datetime"
                          :style (position-popup owner "date-button" "popup")}
                     (calendar {:month month
                                :active-date value
                                :on-change-month #(swap-state! owner assoc :month %)
                                :on-click #(let [value (will-change (date+time % value))]
                                             (when (om/cursor? cur)
                                               (om/update! cur key value))
                                             (when did-change (did-change value)))})
                     (dom/div
                       nil
                       (clock value true 0.61
                              #(let [value (will-change %)]
                                 (when (om/cursor? cur)
                                   (om/update! cur key value))
                                 (when did-change (did-change value))))
                       (clock value false 0.61
                              #(let [value (will-change %)]
                                 (when (om/cursor? cur)
                                   (om/update! cur key value))
                                 (when did-change (did-change value)))))))))))))


(defn calendar-component [cur owner {:keys [className key did-change will-change formatter]
                                     :or {will-change identity}}]
  (let [value (key cur)
        formatter (or formatter date-format)]
    (reify
    om/IRenderState
    (render-state [_ {:keys [month visible]}]
      (dom/div
        #js {:className "calendar-control"}
        (date-button
          #(swap-state! owner update-in [:visible] not)
          (if value
            (ftime/unparse-local formatter value)
            "no date selected"))
        (popup owner
          (when visible
            (dom/div
              #js {:ref "popup"
                   :className "calendar-popup"
                   :style (position-popup owner "date-button" "popup")}
              (calendar {:month month
                         :active-date value
                         :on-change-month #(swap-state! owner assoc :month %)
                         :on-click #(let [value (will-change (date+time % value))]
                                      (when (om/cursor? cur)
                                        (om/update! cur key value))
                                      (when did-change (did-change value)))})))))))))


(defn date-range-component [cur owner {:keys [className start end did-change will-change formatter time?]}]
  (let [will-change (or will-change
                        (fn [changing-start? s e]
                          (cond (nil? s) [e e]
                                (nil? e) [s s]
                                (on-or-before? s e) [s e]
                                changing-start?
                                (let [new-e (date+time s e)]
                                  (if (on-or-before? s new-e)
                                    [s new-e]
                                    [s (time/plus new-e (time/days 1))]))
                                :else
                                (let [new-s (date+time e s)]
                                  (if (on-or-before? new-s e)
                                    [new-s e]
                                    [(time/minus new-s (time/days 1)) e])))))
        start-date (start cur)
        end-date (end cur)
        formatter (or formatter date-format)]
    (reify
      om/IRenderState
      (render-state [_ {:keys [start-month end-month visible]}]
        (dom/div
          #js {:className "calendar-control"}
          (date-button
            #(swap-state! owner update-in [:visible] not)
            (str/join " - " (map (fn [d] (if d (ftime/unparse-local formatter d) "no date"))
                                 [start-date end-date])))
          (popup
            owner
            (when visible
              (dom/div
                #js {:ref "popup"
                     :className "calendar-popup"
                     :style (position-popup owner "date-button" "popup")}
                (dom/div
                  #js {:className "cal-datetime"}
                  (calendar {:month start-month
                             :start-date start-date
                             :end-date end-date
                             :on-change-month #(swap-state! owner assoc :start-month %)
                             :on-click #(let [[start-date end-date]
                                              (will-change true (date+time % start-date) end-date)]
                                          (when (om/cursor? cur)
                                            (om/update! cur start start-date)
                                            (om/update! cur end end-date))
                                          (swap-state! owner assoc :start-month start-date :end-month end-date)
                                          (when did-change (did-change start-date end-date)))})
                  (when time?
                    (dom/div
                      nil
                      (clock start-date true 0.61
                             #(let [[start-date end-date] (will-change true % end-date)]
                                (when (om/cursor? cur)
                                  (om/update! cur start start-date)
                                  (om/update! cur end end-date))
                                (when did-change (did-change start-date end-date))))
                      (clock start-date false 0.61
                             #(let [[start-date end-date] (will-change true % end-date)]
                                (when (om/cursor? cur)
                                  (om/update! cur start start-date)
                                  (om/update! cur end end-date))
                                (when did-change (did-change start-date end-date)))))))
                (dom/div
                  #js {:className "cal-datetime"}
                  (calendar {:month end-month
                             :start-date start-date
                             :end-date end-date
                             :on-change-month #(swap-state! owner assoc :end-month %)
                             :on-click #(let [[start-date end-date]
                                              (will-change false start-date (date+time % end-date))]
                                          (when (om/cursor? cur)
                                            (om/update! cur start start-date)
                                            (om/update! cur end end-date))
                                          (swap-state! owner assoc :start-month start-date :end-month end-date)
                                          (when did-change (did-change start-date end-date)))})
                  (when time?
                    (dom/div
                      nil
                      (clock end-date true 0.61
                             #(let [[start-date end-date] (will-change false start-date %)]
                                (when (om/cursor? cur)
                                  (om/update! cur start start-date)
                                  (om/update! cur end end-date))
                                (when did-change (did-change start-date end-date))))
                      (clock end-date false 0.61
                             #(let [[start-date end-date] (will-change false start-date %)]
                                (when (om/cursor? cur)
                                  (om/update! cur start start-date)
                                  (om/update! cur end end-date))
                                (when did-change (did-change start-date end-date)))))))))))))))


(defn calendar [{:keys [month active-date cursor-date start-date end-date on-change-month on-click]}]
  (let [month (or month active-date start-date (ltime/local-now))
        month (should-be-local (time/first-day-of-the-month month))
        [month active-date cursor-date start-date end-date]
        (map (fn [d] (when d (time/at-midnight d)))
             [month active-date cursor-date start-date end-date])
        [active-date start-date end-date]
        (if (and start-date end-date (time/= start-date end-date))
          [start-date]
          [active-date start-date end-date])]
    (dom/div
      #js {:className "calendar"}
      (table
        #js {:className "table-condensed"}
        [[[{:className "cal-available"
            :onClick #(on-change-month (should-be-local (time/first-day-of-the-month
                                         (time/minus month (time/days 1)))))}
           (dom/i #js {:className "fa fa-arrow-left icon-arrow-left glyphicon glyphicon-arrow-left" })]
          [{:colSpan 5
            :className "month"} (month-name month)]
          [{:className "cal-available"
            :onClick #(on-change-month (should-be-local (time/first-day-of-the-month
                                         (time/plus month (time/days 31)))))}
           (dom/i #js {:className "fa fa-arrow-right icon-arrow-right glyphicon glyphicon-arrow-right"})]]
         ["Su" "Mo" "Tu" "We" "Th" "Fr" "Sa"]]
        (map
          (fn [week]
            (map
              (fn [date]
                [{:className (str "cal-available"
                                  (when (not= (time/month date) (time/month month))
                                    " cal-off")
                                  (when (and active-date (time/= active-date date))
                                    " cal-active")
                                  (when (and cursor-date (time/= cursor-date date))
                                    " cal-active")
                                  (cond (and start-date (time/= start-date date))
                                        " cal-active cal-in-range cal-range-start"
                                        (and end-date (time/= end-date date))
                                        " cal-active cal-range-end"
                                        (and start-date end-date
                                             (time/within? start-date end-date date))
                                        " cal-in-range"))
                  :onClick #(on-click date)}
                 (time/day date)])
              week))
          (partition 7 (date-list month)))))))

(defn clock-component [cur owner {:keys [className key did-change will-change formatter]
                                  :or {will-change identity}}]
  (let [value (key cur)
        formatter (or formatter time-format)]
    (reify
      om/IInitState
      (init-state [_] {:hours? true})
      om/IRenderState
      (render-state [_ {:keys [visible hours?]}]
        (dom/div
          #js {:className "calendar-control"}
          (date-button
            #(swap-state! owner update-in [:visible] not)
            (if value
              (ftime/unparse-local formatter value)
              "no time selected"))
          (popup owner
                 (when visible
                   (dom/div
                     #js {:ref "popup"
                          :className "calendar-popup clock-popup"
                          :style (position-popup owner "date-button" "popup")}
                     (clock value true 1
                            #(let [value (will-change %)]
                               (when (om/cursor? cur)
                                 (om/update! cur key value))
                               (when did-change (did-change value))))
                     (clock value false 1
                            #(let [value (will-change %)]
                               (when (om/cursor? cur)
                                 (om/update! cur key value))
                               (when did-change (did-change value))))))))))))


(defn clock [date hours? scale on-click]
  (let [radius 70
        nub 10
        blob (* 1.4 nub)
        size (* 2 (+ radius nub 2))
        text-shift 5
        half-diam (* js/Math.PI radius)
        half-slice (/ half-diam 12)
        selected (if date
                   (if hours?
                     (let [h (mod (time/hour date) 12)]
                       (if (= 0 h) 12 h))
                     (time/minute date))
                   (if hours? 12 0))
        am? (if date (< (time/hour date) 12) true)
        margin 3
        scale (or scale 1)]
    (dom/svg
      #js {:width (* scale (+ size margin)) :height (* scale (+ size margin))
           :className "clock"}
      (apply
        dom/g #js {:transform (str (svg/scale scale)
                                   (svg/translate margin))}
        (dom/circle
          #js {:r (+ nub radius)
               :className "clock-face"
               :transform (svg/translate (+ nub radius))})
        (concat
          (map
            (fn [n]
              (let [value (if hours?
                            (if (= 0 n) 12 n)
                            (* 5 n))
                    on-click (when on-click
                               #(on-click (if hours?
                                            (at-hour date
                                                     (if am?
                                                       n (+ 12 n)))
                                            (at-minute date value))))]
                (dom/g
                  #js {:transform (str (svg/translate nub)
                                       (svg/rotate (* n 30) radius radius)
                                       (svg/translate radius 0))
                       :className (str "clock-wedge"
                                       (when (= value selected) " clock-active"))
                       :onClick on-click}
                  (dom/path
                    #js {:d (str/join " " ["M" (- half-slice) 0
                                           "L" half-slice 0
                                           "L" 0 radius
                                           "z"])
                         :fill "transparent"
                         :onClick on-click})
                  (dom/circle #js {:r nub :className "clock-nub"
                                   :onClick on-click})
                  (dom/text #js {:transform (svg/rotate (* n 30 -1))
                                 :textAnchor "middle"
                                 :y text-shift
                                 :className "clock-number"
                                 :onClick on-click}
                            value)
                  (dom/line #js {:y1 nub :y2 (- radius nub)
                                 :className "clock-line"
                                 :onClick on-click}))))
            (range 12))
          (when (and (not hours?) (not= 0 (mod selected 5)))
            [(dom/g
               #js {:transform (str (svg/translate nub)
                                    (svg/rotate (* selected 6) radius radius)
                                    (svg/translate radius 0))
                    :className "clock-active"}
               (dom/circle #js {:r (/ nub 2) :className "clock-nub"})
               (dom/line #js {:y1 (/ nub 2) :y2 (- radius nub)
                              :className "clock-line"}))])))
      (when hours?
        (dom/g
          #js {:transform (str (svg/scale scale) (svg/translate (- size blob) blob))
               :className (str "clock-meridiem"
                               (if am?  " clock-active"))
               :onClick (when (and date on-click)
                          (when date #(on-click (at-hour date (mod (time/hour date) 12)))))}
          (dom/circle
            #js {:r blob})
          (dom/text
            #js {:textAnchor "middle"
                 :y text-shift}
            "AM")))
      (when hours?
        (dom/g
          #js {:transform (str (svg/scale scale) (svg/translate (+ margin (- size blob)) (- size blob)))
               :className (str "clock-meridiem"
                               (if (not am?)
                                 " clock-active"))
               :onClick (when (and date on-click)
                          (when date #(on-click (at-hour date (+ 12 (mod (time/hour date) 12))))))}
          (dom/circle
            #js {:r blob})
          (dom/text
            #js {:textAnchor "middle"
                 :y text-shift}
            "PM"))))))

