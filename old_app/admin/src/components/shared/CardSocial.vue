<template>
  <div class="col-md-4 col-sm-12 col-xs-12">
    <q-item :style="`background-color: ${lightColor}`" class="q-pa-none q-ml-xs">
      <q-item-section v-if="iconPosition === 'left'" side :style="`background-color: ${colorBg}`" class=" q-pa-lg q-mr-none text-white">
        <q-icon :name="icon" :color="colorIcon" size="24px"></q-icon>
      </q-item-section>
      <q-item-section class=" q-pa-md q-ml-none" :style="`color: ${colorText}`">
        <q-item-label class="text-h6 text-weight-bolder" :style="`color: ${colorText}`">{{ value }}</q-item-label>
        <q-item-label>{{ label }}</q-item-label>
      </q-item-section>
      <q-item-section v-if="iconPosition === 'right'" side class="q-mr-md text-white">
        <q-icon :name="icon" color="white" size="44px"></q-icon>
      </q-item-section>
    </q-item>
  </div>
</template>

<script>
import { colors } from 'quasar'

export default {
  name: "CardSocial",
  props: {
    iconPosition: {
      required: false,
      default: "left"
    },
    value: {
      required: true,
      default: ""
    },
    label: {
      required: true,
      default: ""
    },
    icon: {
      required: false,
      default: "person"
    },
    colorText: {
      required: false,
      default: "#000"
    },
    colorIcon: {
      required: false,
      default: "white"
    },
    colorBg: {
      required: false,
      default: "#0072dc"
    }
  },
  computed: {
    lightColor() {
      return colors.lighten(this.colorBg, +80)
    }
  }
};
</script>
