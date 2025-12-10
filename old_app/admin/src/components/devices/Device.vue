<template>
  <q-page v-if="device" class="q-pa-md">
    <q-list bordered padding>
      <q-item v-show="device.serialNumber">
        <q-item-section>
          <q-item-label overline>Serial Number</q-item-label>
          <q-item-label class="q-pa-md">{{ device.serialNumber }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />

      <q-item v-show="device.organizationId">
        <q-item-section>
          <q-item-label overline>Organization</q-item-label>
          <q-item-label class="q-pa-md">{{
            device.organization.name
          }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item v-show="device.deviceConfigId">
        <q-separator spaced />
        <q-item-section>
          <q-item-label overline>Config</q-item-label>
          <q-item-label class="q-pa-md">{{
            device.deviceConfig ? device.deviceConfig.configName : ""
          }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item v-show="device.referenceSample">
        <q-item-section>
          <q-item-label overline>Reference Sample</q-item-label>
          <v-jsoneditor
            v-model="device.referenceSample"
            :options="{ mode: 'view' }"
            height="250px"
          />
        </q-item-section>
        <q-item-section side>
          <q-btn
            color="warning"
            size="sm"
            label="Clear"
            @click.native.stop="clearReferenceSample(device.id)"
          />
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item v-if="device.lastCalibrationAt">
        <q-item-section>
          <q-item-label overline>Last Calibration At</q-item-label>
          <q-item-label class="q-pa-md">{{
            device.lastCalibrationAt
          }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Updated At</q-item-label>
          <q-item-label class="q-pa-md">{{ device.updatedAt }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Created At</q-item-label>
          <q-item-label class="q-pa-md">{{ device.createdAt }}</q-item-label>
        </q-item-section>
      </q-item>
    </q-list>

    <div class="row q-pa-md">
      <div class="col">
        <q-btn
          class="gt-xs"
          color="negative"
          size="sm"
          @click.native.stop="confirmDelete(device.id)"
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
          @click.native.stop="editRecord(device.id)"
        />
      </div>
    </div>

    <q-dialog v-model="showDelete" persistent>
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm"
            >Are you sure you want to delete
            <strong>"{{ device.serialNumber }}"</strong>?</span
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
            @click.native="deleteSelected(device.id)"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script>
import { mapGetters, mapActions } from "vuex";
import { showMixin } from "@/mixins/crud/pagination";
import VJsoneditor from "v-jsoneditor/src/index";

export default {
  name: "Device",
  components: {
    VJsoneditor,
  },
  mixins: [showMixin("device")],
  props: {
    id: {
      type: String,
      default: "",
    },
  },
  computed: {
    ...mapGetters({
      byId: "devices/byId",
    }),
    device() {
      return this.byId(this.id);
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
      loadById: "devices/loadById",
      delete: "devices/delete",
    }),
    clearReferenceSample(id) {
      this.$store.dispatch("devices/clearReferenceSample", id).then(() => {
        this.loadById(id);
      });
    },
  },
};
</script>

<style lang="scss" scoped></style>
