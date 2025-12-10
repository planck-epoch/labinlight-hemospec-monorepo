<template>
  <q-page class="q-pa-md">
    <q-form v-if="form" class="q-gutter-md" @submit="onSubmit" @reset="onReset">
      <q-select
        v-model="form.organizationId"
        use-input
        input-debounce="0"
        :options="organizationsAvailable"
        option-value="id"
        option-label="name"
        emit-value
        map-options
        label="Organization *"
      />

      <q-input v-model="form.serialNumber" label="serial_number *" />

      <q-select
        v-model="form.deviceConfigId"
        use-input
        input-debounce="0"
        :options="deviceConfigsAvailable"
        option-value="id"
        option-label="configName"
        emit-value
        map-options
        label="Config"
      />

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
import { DEFAULT_DEVICE_VALUES } from "@/graphql/queries/devices";
import { formMixin } from "@/mixins/crud/form";
import { mapGetters, mapActions } from "vuex";
import _ from "lodash";

export default {
  name: "DeviceForm",
  mixins: [formMixin("device")],
  data: function () {
    return {
      organizationsAvailable: [],
      deviceConfigsAvailable: [],
      form: JSON.parse(JSON.stringify(DEFAULT_DEVICE_VALUES)),
    };
  },
  computed: {
    ...mapGetters({
      organizations: "organizations/all",
      deviceConfigs: "deviceConfigs/all"
    }),
    device() {
      return this.byId(this.id);
    },
  },
  watch: {
    device() {
      if (this.id) {
        this.form = Object.assign({}, this.device);
      }
    },
    organizations() {
      this.organizationsAvailable = this.organizations;
    },
    deviceConfigs() {
      this.deviceConfigsAvailable = this.deviceConfigs;
    },
  },
  mounted() {
    this.loadAllOrganizations({ force: false, limit: 0 });
    this.loadAllDeviceConfigs({ force: false, limit: 0 });
  },
  methods: {
    ...mapActions({
      loadAllOrganizations: "organizations/loadAll",
      loadAllDeviceConfigs: "deviceConfigs/loadAll",
    }),
    onSubmit() {
      const sanitizedForm = _.pick(this.form, _.keys(DEFAULT_DEVICE_VALUES));
      sanitizedForm.organizationId = Number(sanitizedForm.organizationId);
      sanitizedForm.deviceConfigId = Number(sanitizedForm.deviceConfigId);
      let vm = this;
      this.save({ id: parseInt(this.id), form: sanitizedForm }).then(function (
        response
      ) {
        if (response) {
          const responseId = response.data.devices.id;
          vm.$router.push({ name: "DeviceShow", params: { id: responseId } });
        }
      });
    },
    onReset() {
      this.form = JSON.parse(JSON.stringify(DEFAULT_DEVICE_VALUES));
    },
    onCancel() {
      if (this.id) {
        this.$router.push({
          name: "DeviceShow",
          params: { id: parseInt(this.id) },
        });
      } else {
        this.$router.push({ name: "DeviceList" });
      }
    },
  },
};
</script>

<style lang="scss" scoped></style>
