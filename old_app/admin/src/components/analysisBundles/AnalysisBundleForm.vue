<template>
  <q-page class="q-pa-md">
    <q-form v-if="form" class="q-gutter-md" @submit="onSubmit" @reset="onReset">
      <q-input v-model="form.name" label="name *" />

      <q-input v-model="form.code" label="code *" />

      <q-select
        v-model="form.analysisTestIds"
        use-input
        input-debounce="0"
        :options="analysisTestsAvailable"
        :multiple="true"
        option-value="id"
        option-label="name"
        emit-value
        map-options
        label="Tests *"
      />

      <q-checkbox left-label v-model="form.enabled" label="enabled" />

      <q-checkbox left-label v-model="form.default" label="Default" />

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
import { DEFAULT_ANALYSIS_BUNDLE_VALUES } from "@/graphql/queries/analysisBundles";
import { formMixin } from "@/mixins/crud/form";
import { mapGetters, mapActions } from "vuex";
import _ from "lodash";

export default {
  name: "AnalysisBundleForm",
  mixins: [formMixin("analysisBundle")],
  data: function () {
    return {
      analysisTestsAvailable: [],
      form: JSON.parse(JSON.stringify(DEFAULT_ANALYSIS_BUNDLE_VALUES)),
    };
  },
  computed: {
    ...mapGetters({
      analysisTests: "analysisTests/all",
    }),
    analysisBundle() {
      return this.byId(this.id);
    },
  },
  watch: {
    analysisTests() {
      this.analysisTestsAvailable = this.analysisTests.map((obj) => ({
        id: obj.id,
        name: obj.name,
      }));
    },
    analysisBundle() {
      if (this.id) {
        this.form = Object.assign({}, this.analysisBundle);
      }
    },
  },
  mounted() {
    this.loadAllAnalysisTests({ force: false, limit: 0 });
  },
  methods: {
    ...mapActions({
      loadAllAnalysisTests: "analysisTests/loadAll",
    }),
    onSubmit() {
      const sanitizedForm = _.pick(
        this.form,
        _.keys(DEFAULT_ANALYSIS_BUNDLE_VALUES)
      );
      let vm = this;
      this.save({ id: parseInt(this.id), form: sanitizedForm }).then(function (
        response
      ) {
        if (response) {
          const responseId = response.data.analysisBundles.id;
          vm.$router.push({
            name: "AnalysisBundleShow",
            params: { id: responseId },
          });
        }
      });
    },
    onReset() {
      this.form = JSON.parse(JSON.stringify(DEFAULT_ANALYSIS_BUNDLE_VALUES));
    },
    onCancel() {
      if (this.id) {
        this.$router.push({
          name: "AnalysisBundleShow",
          params: { id: parseInt(this.id) },
        });
      } else {
        this.$router.push({ name: "AnalysisBundleList" });
      }
    },
  },
};
</script>

<style lang="scss" scoped></style>
