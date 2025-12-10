<template>
  <q-page v-if="analysisBundle" class="q-pa-md">
    <q-list bordered padding>
      <q-item v-show="analysisBundle.name">
        <q-item-section>
          <q-item-label overline>Name</q-item-label>
          <q-item-label class="q-pa-md">{{ analysisBundle.name }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-separator spaced />

      <q-item v-show="analysisBundle.code">
        <q-item-section>
          <q-item-label overline>Code</q-item-label>
          <q-item-label class="q-pa-md">{{ analysisBundle.code }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item v-show="analysisBundle.analysisTests">
        <q-item-section>
          <q-item-label overline>Tests</q-item-label>
          <q-item-label class="q-pa-md">{{
            analysisTestsNames(analysisBundle.analysisTestIds)
          }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-item v-show="typeof analysisBundle.default !== 'undefined'">
        <q-item-section>
          <q-item-label overline>Default</q-item-label>
          <q-item-label class="q-pa-md">{{
            analysisBundle.default ? "Yes" : "No"
          }}</q-item-label>
        </q-item-section>
      </q-item>
      <q-item v-show="analysisBundle.enabled">
        <q-item-section>
          <q-item-label overline>Enabled</q-item-label>
          <q-item-label class="q-pa-md">{{
            analysisBundle.enabled
          }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Updated At</q-item-label>
          <q-item-label class="q-pa-md">{{
            analysisBundle.updatedAt
          }}</q-item-label>
        </q-item-section>
      </q-item>

      <q-separator spaced />

      <q-item>
        <q-item-section>
          <q-item-label overline>Created At</q-item-label>
          <q-item-label class="q-pa-md">{{
            analysisBundle.createdAt
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
          @click.native.stop="confirmDelete(analysisBundle.id)"
        >
          Delete
        </q-btn>
      </div>

      <div class="col-6"></div>

      <div class="col">
        <q-btn
          class="gt-xs float-right"
          color="primary"
          size="sm"
          label="Edit"
          @click.native.stop="editRecord(analysisBundle.id)"
        />
      </div>
    </div>

    <q-dialog v-model="showDelete" persistent>
      <q-card>
        <q-card-section class="row items-center">
          <span class="q-ml-sm"
            >Are you sure you want to delete
            <strong>"{{ analysisBundle.name }}"</strong>?</span
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
            @click.native="deleteSelected(analysisBundle.id)"
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
  name: "AnalysisBundle",
  mixins: [showMixin("analysisBundle")],
  props: {
    id: {
      type: String,
      default: "",
    },
  },
  computed: {
    ...mapGetters({
      byId: "analysisBundles/byId",
    }),
    analysisBundle() {
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
      loadById: "analysisBundles/loadById",
      delete: "analysisBundles/delete",
    }),
    analysisTestsNames(analysisTestIds) {
      let that = this;
      return analysisTestIds
        .map((id) => {
          if (that.$store.state.analysisTests.entities[id]) {
            return that.$store.state.analysisTests.entities[id].name;
          }
        })
        .join(", ");
    },
  },
};
</script>

<style lang="scss" scoped></style>
