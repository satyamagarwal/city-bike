<template>
  <div class="osg-header__content">
    <div class="osg-none osg-block-breakpoint-large osg-full-height">
      <div class="osg-grid osg-grid--gap osg-full-height">
        <div class="osg-grid__column--5">
          <div
            class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-text-size-golf"
          >
            Oslo Bysykkle: Stasjon søk
          </div>
        </div>
        <div class="osg-grid__column--7">
          <div class="osg-full-height osg-flex osg-flex-justify-content-flex-end osg-flex-align-items-center">
            <input
              v-model="value"
              class="osg-input__input"
              type="text"
              autocomplete="on"
              aria-label="Label text"
              placeholder="Søk etter et address"
              data-testid="search-address"
              @input="searchParam"
              @change="searchParam"
            >
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "PageHeader",
  emits: ['search'],
  data() {
    return {
      value: ''
    };
  },
  watch: {
    value() {
      this.$emit("search", this.value);
    }
  },
  methods: {
    searchParam() {
      this.$emit("search", this.value)
    }
  }
}
</script>
