import Vue from 'vue'

import './styles/quasar.scss'
import '@quasar/extras/roboto-font/roboto-font.css'
import '@quasar/extras/material-icons/material-icons.css'
import {
  Quasar,
  QLayout,
  QHeader,
  QFooter,
  QDrawer,
  QPageContainer,
  QPage,
  QToolbar,
  QToolbarTitle,
  QBtn,
  QIcon,
  QList,
  QItem,
  QItemSection,
  QItemLabel,
  QSeparator,
  QCard,
  QCardSection,
  QCardActions,
  QForm,
  QInput,
  QSelect,
  QCheckbox,
  QDialog,
  QPagination,
  QDate,
  QTime,
  QPopupProxy,
  Ripple,
  ClosePopup,
  Loading,
  Notify
} from 'quasar'

Vue.use(Quasar, {
  components: {
    QLayout,
    QHeader,
    QFooter,
    QDrawer,
    QPageContainer,
    QPage,
    QToolbar,
    QToolbarTitle,
    QBtn,
    QIcon,
    QList,
    QItem,
    QItemSection,
    QItemLabel,
    QSeparator,
    QCard,
    QCardSection,
    QCardActions,
    QForm,
    QInput,
    QSelect,
    QCheckbox,
    QDialog,
    QPagination,
    QDate,
    QTime,
    QPopupProxy
  },
  directives: {
    Ripple,
    ClosePopup,
    QPopupProxy
  },
  plugins: {
    Loading,
    Notify
  },
  config: {
    loading: {},
    notify: {
      position: 'bottom',
      timeout: 3500,
      color: 'negative',
      textColor: 'white',
      actions: [{ icon: 'close', color: 'white' }]
    }
  }
 })
