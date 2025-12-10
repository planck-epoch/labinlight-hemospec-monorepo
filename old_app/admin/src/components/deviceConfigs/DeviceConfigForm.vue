<template>
  <q-page class="q-pa-md">
    <q-form v-if="form" class="q-gutter-md" @submit="onSubmit" @reset="onReset">
      <q-input v-model="form.configName" label="Name *" />
      <q-input
        v-model="form.numRepeats"
        type="number"
        label="Number of scans to Avg *"
      />

      <!-- Sections Editing -->
      <div>
        <div class="text-h6 q-mt-md q-mb-sm">Sections</div>
        <div
          v-for="(section, idx) in sectionsData"
          :key="idx"
          class="q-pa-sm q-mb-sm bg-grey-2"
        >
          <div class="row q-col-gutter-md">
            <div class="col">
              <q-select
                v-model="section.section_scan_type"
                :options="scanTypeOptions"
                label="Scan Type"
                dense
                emit-value
                map-options
              />
            </div>
            <div class="col">
              <q-input
                v-model.number="section.wavelength_start_nm"
                label="Spectral Range Start"
                type="number"
                dense
              />
            </div>
            <div class="col">
              <q-input
                v-model.number="section.wavelength_end_nm"
                label="Spectral Range End"
                type="number"
                dense
              />
            </div>
            <div class="col">
              <q-select
                v-model="section.width_px"
                :options="widthPxOptions"
                label="Width (nm)"
                dense
                emit-value
                map-options
              />
            </div>
            <div class="col">
              <q-select
                v-model="section.exposure_time"
                :options="exposureTimeOptions"
                label="Exposure Time (ms)"
                dense
                emit-value
                map-options
              />
            </div>
            <div class="col">
              <q-input
                v-model.number="section.num_patterns"
                label="Digital Resolution"
                type="number"
                dense
              />
            </div>
            <div class="col-auto flex flex-center">
              <q-btn
                icon="delete"
                color="negative"
                dense
                flat
                @click="removeSection(idx)"
                :disable="sectionsData.length === 1"
              />
            </div>
          </div>
        </div>
        <q-btn
          label="Add Section"
          color="primary"
          flat
          icon="add"
          size="sm"
          @click="addSection"
          class="q-mb-md"
          :disable="sectionsData.length >= 5"
        />
      </div>
      <!-- End Sections Editing -->

      <div class="row q-pa-md">
        <div class="col">
          <q-btn
            label="Cancel"
            color="primary"
            flat
            size="sm"
            class="q-ml-sm"
            @click="onCancel"
          />
        </div>

        <div class="col-6" />

        <div class="col">
          <q-btn
            label="Submit"
            type="submit"
            color="primary"
            size="sm"
            class="float-right"
          />
        </div>
      </div>
    </q-form>
  </q-page>
</template>

<script>
import { DEFAULT_DEVICE_CONFIG_VALUES } from "@/graphql/queries/deviceConfigs";
import { formMixin } from "@/mixins/crud/form";
import { mapGetters, mapActions } from "vuex";
import _ from "lodash";

// Helper for default section
function defaultSection() {
  return {
    section_scan_type: 0,
    wavelength_start_nm: 900,
    wavelength_end_nm: 1700,
    width_px: 6,
    exposure_time: 0,
    num_patterns: 228,
  };
}

// Options for dropdowns
const scanTypeOptions = [
  { label: "Column", value: 0 },
  { label: "Hadamard", value: 1 },
];
const exposureTimeOptions = [
  { label: "0.635", value: 0 },
  { label: "1.27", value: 1 },
  { label: "2.54", value: 2 },
  { label: "5.08", value: 3 },
  { label: "15.24", value: 4 },
  { label: "30.48", value: 5 },
  { label: "60.96", value: 6 },
];
const widthPxOptions = [
  { label: "2.34", value: 2 },
  { label: "3.51", value: 3 },
  { label: "4.68", value: 4 },
  { label: "5.85", value: 5 },
  { label: "7.03", value: 6 },
  { label: "8.2", value: 7 },
  { label: "9.37", value: 8 },
  { label: "10.54", value: 9 },
  { label: "11.71", value: 10 },
  { label: "12.88", value: 11 },
  { label: "14.05", value: 12 },
  { label: "15.22", value: 13 },
  { label: "16.39", value: 14 },
  { label: "17.56", value: 15 },
  { label: "18.74", value: 16 },
  { label: "19.91", value: 17 },
  { label: "21.08", value: 18 },
  { label: "22.25", value: 19 },
  { label: "23.42", value: 20 },
  { label: "24.59", value: 21 },
  { label: "25.76", value: 22 },
  { label: "26.93", value: 23 },
  { label: "28.1", value: 24 },
  { label: "29.27", value: 25 },
  { label: "30.44", value: 26 },
  { label: "31.62", value: 27 },
  { label: "32.79", value: 28 },
  { label: "33.96", value: 29 },
  { label: "35.13", value: 30 },
  { label: "36.3", value: 31 },
  { label: "37.47", value: 32 },
  { label: "38.64", value: 33 },
  { label: "39.81", value: 34 },
  { label: "40.98", value: 35 },
  { label: "42.15", value: 36 },
  { label: "43.33", value: 37 },
  { label: "44.5", value: 38 },
  { label: "45.67", value: 39 },
  { label: "46.84", value: 40 },
  { label: "48.01", value: 41 },
  { label: "49.18", value: 42 },
  { label: "50.35", value: 43 },
  { label: "51.52", value: 44 },
  { label: "52.69", value: 45 },
  { label: "53.86", value: 46 },
  { label: "55.04", value: 47 },
  { label: "56.21", value: 48 },
  { label: "57.38", value: 49 },
  { label: "58.55", value: 50 },
  { label: "59.72", value: 51 },
  { label: "60.89", value: 52 },
];

export default {
  name: "DeviceConfigForm",
  mixins: [formMixin("deviceConfig")],
  data: function () {
    return {
      organizationsAvailable: [],
      form: JSON.parse(JSON.stringify(DEFAULT_DEVICE_CONFIG_VALUES)),
      sectionsData: [defaultSection()],
      scanTypeOptions,
      exposureTimeOptions,
      widthPxOptions,
    };
  },
  computed: {
    ...mapGetters({}),
    deviceConfig() {
      return this.byId(this.id);
    },
  },
  watch: {
    deviceConfig() {
      if (this.id) {
        this.form = Object.assign({}, this.deviceConfig);
        // Parse sections field into sectionsData
        try {
          this.sectionsData = this.deviceConfig.sections
            ? JSON.parse(this.deviceConfig.sections)
            : [defaultSection()];
        } catch (e) {
          this.sectionsData = [defaultSection()];
        }
      }
    },
  },
  mounted() {
    // If editing, parse sections
    if (this.id && this.deviceConfig) {
      try {
        this.sectionsData = this.deviceConfig.sections
          ? JSON.parse(this.deviceConfig.sections)
          : [defaultSection()];
      } catch (e) {
        this.sectionsData = [defaultSection()];
      }
    }
  },
  methods: {
    ...mapActions({}),
    addSection() {
      this.sectionsData.push(defaultSection());
    },
    removeSection(idx) {
      if (this.sectionsData.length > 1) {
        this.sectionsData.splice(idx, 1);
      }
    },
    onSubmit() {
      // Prevent saving if no section exists
      if (!this.sectionsData.length) {
        this.$q.notify({
          type: "negative",
          message: "At least one section is required.",
        });
        return;
      }
      // Serialize sectionsData into sections field
      this.form.sections = JSON.stringify(this.sectionsData);
      const sanitizedForm = _.pick(
        this.form,
        _.keys(DEFAULT_DEVICE_CONFIG_VALUES)
      );
      sanitizedForm.numRepeats = Number(sanitizedForm.numRepeats);
      let vm = this;
      this.save({ id: parseInt(this.id), form: sanitizedForm }).then(function (
        response
      ) {
        if (response) {
          const responseId = response.data.deviceConfigs.id;
          vm.$router.push({
            name: "DeviceConfigShow",
            params: { id: responseId },
          });
        }
      });
    },
    onReset() {
      this.form = JSON.parse(JSON.stringify(DEFAULT_DEVICE_CONFIG_VALUES));
      this.sectionsData = [defaultSection()];
    },
    onCancel() {
      if (this.id) {
        this.$router.push({
          name: "DeviceConfigShow",
          params: { id: parseInt(this.id) },
        });
      } else {
        this.$router.push({ name: "DeviceConfigList" });
      }
    },
  },
};
</script>

<style lang="scss" scoped></style>
