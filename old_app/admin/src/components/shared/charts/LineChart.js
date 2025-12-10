import Chart from 'chart.js'
import { mixins, generateChart } from 'vue-chartjs'
const { reactiveProp } = mixins

Chart.defaults.LineWithLine = Chart.defaults.line
Chart.controllers.LineWithLine = Chart.controllers.line.extend({
  draw(ease) {
    Chart.controllers.line.prototype.draw.call(this, ease);
    if (this.chart.tooltip._active && this.chart.tooltip._active.length) {
       var activePoint = this.chart.tooltip._active[0],
           ctx = this.chart.ctx,
           x = activePoint.tooltipPosition().x,
           topY = this.chart.scales['y-axis-0'].top,
           bottomY = this.chart.scales['y-axis-0'].bottom;

       // draw line
       ctx.save();
       ctx.beginPath();
       ctx.moveTo(x, topY);
       ctx.lineTo(x, bottomY);
       ctx.lineWidth = 2;
       ctx.strokeStyle = '#ceccda';
       ctx.stroke();
       ctx.restore();
    }
  }
})

const CustomLine = generateChart('custom-line', 'LineWithLine')

export default {
  extends: CustomLine,
  mixins: [reactiveProp],
  props: ['options'],
  mounted () {
    // this.chartData is created in the mixin.
    // If you want to pass options please create a local options object
    this.renderChart(this.chartData, this.options)
  },
}
