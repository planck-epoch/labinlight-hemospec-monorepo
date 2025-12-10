import moment from 'moment'

export default {
  methods: {
    $formatFullDate: function(value) {
      if( !value) return '';

      return moment(value).format('YYYY-MM-DD - HH:mm:ss');
    }
  }
}
