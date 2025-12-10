<template>
  <q-page class="q-pa-sm">
    <q-card class="bg-transparent no-shadow no-border">
      <q-card-section class="q-py-lg">
        <div class="row q-col-gutter-xl q-py-lg">
          <CardSocial
            icon="connect_without_contact"
            iconPosition="left"
            label="New analyses last 24 hours"
            :value="analysesLast1"
            colorBg="#027BE3"
          />
          <CardSocial
            icon="connect_without_contact"
            iconPosition="left"
            label="New analyses last 15 days"
            :value="analysesLast15"
            colorBg="#027BE3"
          />
          <CardSocial
            icon="connect_without_contact"
            iconPosition="left"
            label="New analyses last 30 days"
            :value="analysesLast30"
            colorBg="#027BE3"
          />
        </div>

        <div class="row q-col-gutter-xl q-py-lg">
          <div class="col-12">
            <div class="chart-holder">
              <line-chart v-if="analysesChart" :chart-data="analysesChart" :options="analysesChartOptions"></line-chart>
            </div>
          </div>
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script>
  import gql from 'graphql-tag'

  import CardSocial from '@/components/shared/CardSocial'
  import LineChart from '@/components/shared/charts/LineChart.js'

  export default {
    name: 'Home',
    components: {
      CardSocial,
      LineChart
    },
    data () {
      return {
        analysesLast1: 0,
        analysesLast15: 0,
        analysesLast30: 0,

        analysesChart: null,

        analysesChartOptions: {
          responsive:true,
          maintainAspectRatio: false,
          tooltips: {
            titleFontSize: 14,
            bodyFontSize: 14,
            bodySpacing: 4,
            xPadding: 20,
            yPadding: 20,
            intersect: false,
            axis: 'x'
          },
          scales: {
            xAxes: [{
              gridLines: {
                display: false
              },
              ticks: {
                fontColor: "#a9a8ae",
                fontSize: 11
              }
            }],
            yAxes: [{
              ticks: {
                fontColor: "#807f81",
                fontSize: 12
              }
            }]
          },
          scaleLabel: {
            fontColor: '#fff'
          }
        }
      }
    },
    mounted() {
      this.setStatsAnalyses()
    },
    methods: {
      async setStatsAnalyses() {
        const checkLogin = await this.$apollo.query({
          query: gql`
            query {
              user: me(resourceName: "admin"){
                token
              }
            }
          `,
        })
        if (checkLogin.data.user == null) {
          this.$router.push({name: 'Login', params: { prevPath: '/' }})
        } else {
          const response = await this.$apollo.query({
            query: gql`
              query {
                analysesLast1: statsAnalyses(days: 1)
                analysesLast15: statsAnalyses(days: 15)
                analysesLast30: statsAnalyses(days: 30)

                analysesChart: statsAnalysesPerDay(days: 30) {
                  day
                  value
                }
              }
            `,
          })
          this.analysesLast1 = response.data.analysesLast1
          this.analysesLast15 = response.data.analysesLast15
          this.analysesLast30 = response.data.analysesLast30

          this.analysesChart = {
                              labels: response.data.analysesChart.map(x => x.day),
                              maintainAspectRatio: false,
                              datasets: [
                                {
                                  label: 'All Analyses Last 30 Days',
                                  borderColor: '#027BE3',
                                  fill: false,
                                  data: response.data.analysesChart.map(x => x.value)
                                }
                              ],
                            }
        }
      }
    }
  }
</script>

<style lang="scss" scoped>
.chart-holder {
  max-height: 100px;
}
</style>
