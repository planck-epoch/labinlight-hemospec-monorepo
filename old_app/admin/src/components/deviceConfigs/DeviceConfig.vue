<template>
  <q-page v-if="deviceConfig" class="q-pa-md">
    <q-list bordered padding>
      <q-item v-show="deviceConfig.configName">
        <q-item-section>
          <q-item-label overline>Name</q-item-label>
          <q-item-label class="q-pa-md">{{
            deviceConfig.configName
          }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item v-show="deviceConfig.configName">
        <q-item-section>
          <q-item-label overline>Num Scans Avg.</q-item-label>
          <q-item-label class="q-pa-md">{{
            deviceConfig.numRepeats
          }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />

      <q-item>
        <q-item-section class="col-xs-2 col-sm-2">
          <q-item-label overline>Scan Type</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-2 col-sm-2">
          <q-item-label overline>Spectral Range Start</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-3 col-sm-2">
          <q-item-label overline>Spectral Range End</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-3 col-sm-1">
          <q-item-label overline>Width (nm)</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-3 col-sm-1">
          <q-item-label overline>Exposure Time (ms)</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-3 col-sm-1">
          <q-item-label overline>Digital Resolution</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />
      <q-item
        v-for="(section, index) in deviceConfig.sectionsData"
        :key="index"
        v-ripple
        class="crud-item"
      >
        <q-item-section class="col-xs-2 col-sm-2">
          <q-item-label>{{
            $parseScanType(section.section_scan_type)
          }}</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-2 col-sm-2">
          <q-item-label>{{ section.wavelength_start_nm }}</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-2 col-sm-2">
          <q-item-label>{{ section.wavelength_end_nm }}</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-2 col-sm-1">
          <q-item-label>{{ $parseWidth(section.width_px) }}</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-2 col-sm-1">
          <q-item-label>{{
            $parseExposureTime(section.exposure_time)
          }}</q-item-label>
        </q-item-section>

        <q-item-section class="col-xs-2 col-sm-1">
          <q-item-label>{{ section.num_patterns }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item style="margin-top: 100px">
        <q-item-section>
          <q-item-label overline>Updated At</q-item-label>
          <q-item-label class="q-pa-md">{{
            deviceConfig.updatedAt
          }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Created At</q-item-label>
          <q-item-label class="q-pa-md">{{
            deviceConfig.createdAt
          }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>

    <div class="row q-pa-md">
      <div class="col">
        <q-btn
          class="gt-xs"
          color="negative"
          size="sm"
          @click.native.stop="confirmDelete(deviceConfig.id)"
        >
          Delete
        </q-btn>
      </div>

      <div class="col-6"></div>

      <div class="col">
        <q-btn
          class="gt-xs float-right"
          color="primary"
          label="Edit"
          size="sm"
          @click.native.stop="editRecord(deviceConfig.id)"
        />
      </div>
    </div>

    <q-dialog v-model="showDelete" persistent>
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm"
            >Are you sure you want to delete
            <strong>"{{ deviceConfig.configName }}"</strong>?</span
          >
        </q-card-section>

        <q-card-actions align="right">
          <q-btn
            flat
            label="Cancel"
            color="primary"
            @click.native="cancelSelected()"
          />
          <q-btn
            flat
            label="Delete"
            color="negative"
            @click.native="deleteSelected(deviceConfig.id)"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script>
import { mapGetters, mapActions } from "vuex";
import { showMixin } from "@/mixins/crud/pagination";

export default {
  name: "DeviceConfig",
  mixins: [showMixin("deviceConfig")],
  props: {
    id: {
      type: String,
      default: "",
    },
  },
  computed: {
    ...mapGetters({
      byId: "deviceConfigs/byId",
    }),
    deviceConfig() {
      var cfg = this.byId(this.id);
      if (cfg) {
        cfg.sectionsData = JSON.parse(cfg.sections);
      }
      return cfg;
    },
  },
  mounted() {
    let that = this;
    this.loadById(this.id).then(function (response) {
      if (!response) {
        that.$router.push({ name: "NotFound" });
      }
    });
  },
  methods: {
    ...mapActions({
      loadById: "deviceConfigs/loadById",
      delete: "deviceConfigs/delete",
    }),
  },
};
</script>

<style lang="scss" scoped></style>
