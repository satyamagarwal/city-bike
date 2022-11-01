<template>
  <header class="osg-header">
    <div class="osg-header__logo">
      <figure class="osg-logo">
        <img
          src="./../assets/oslo_logo.svg"
          alt="Oslo Kommune logo"
        >
      </figure>
    </div>
    <PageHeader @search="searchParam" />
  </header>
  <div class="osg-tabs">
    <div
      class="osg-tabs__triggers"
      role="tablist"
      aria-labelledby="tablist-1"
    >
      <button
        id="tab-1"
        class="osg-tabs__trigger"
        role="tab"
        aria-controls="tabpanel-1"
        aria-selected="true"
      >
        Kart
      </button>
      <button
        id="tab-2"
        class="osg-tabs__trigger"
        role="tab"
        aria-controls="tabpanel-2"
        aria-selected="false"
        tabindex="-1"
        data-testid="table-tab"
      >
        Tabell
      </button>
    </div>
    <div class="osg-tabs__tabs">
      <div
        id="tabpanel-1"
        role="tabpanel"
        aria-labelledby="tab-1"
        class="osg-tabs__tab osg-tabs__tab--active"
      >
        <ShowMap :stations-and-statuses="searchedStationsAndStatuses" />
      </div>
      <div
        id="tabpanel-2"
        role="tabpanel"
        aria-labelledby="tab-2"
        class="osg-tabs__tab"
      >
        <ShowList
          tabindex="0"
          :stations-and-statuses="searchedStationsAndStatuses"
          :search-param="searchTerm"
        />
      </div>
    </div>
  </div>
</template>

<script>
import PageHeader from "./PageHeader.vue";
import ShowList from "./ShowList.vue";
import ShowMap from "./ShowMap.vue";
import {config} from 'icefog'
import {OsgTabs} from "styleguide/src/components/tabs/tabs";

const host = config.isProd ? '' : 'http://localhost:8080'

document.addEventListener("DOMContentLoaded", function () {
  OsgTabs.init();
});

export default {
  name: "AppContainer",
  components: {
    ShowMap,
    PageHeader,
    ShowList
  },
  data() {
    return {
      defaultStationsAndStatuses: [],
      searchedStationsAndStatuses: [],
      searchTerm: '',
    }
  },
  mounted() {
    this.getInfoAndStatus()

    setInterval(this.getInfoAndStatus, 10000);
  },
  methods: {
    async getInfoAndStatus() {
      const response = await fetch(`${host}/station-status`)
      const content = response.ok ? await response.json() : []
      const sortedList = content.sort((station1, station2) => station1.info.name.localeCompare(station2.info.name))

      this.defaultStationsAndStatuses = sortedList

      if (this.searchedStationsAndStatuses.length === 0) {
        this.searchedStationsAndStatuses = sortedList
      }
    },
    searchParam(param) {
      if (param) {
        const lowerCasedSearch = this.searchTerm.toLocaleLowerCase()

        this.searchTerm = param
        this.searchedStationsAndStatuses = this
            .defaultStationsAndStatuses
            .filter((stationAndStatus) =>
                stationAndStatus.info.name.toLocaleLowerCase().includes(lowerCasedSearch) ||
                stationAndStatus.info.address.includes(lowerCasedSearch)
            )
      } else {
        this.searchTerm = ''
        this.searchedStationsAndStatuses = this.defaultStationsAndStatuses
      }
    }
  }
}
</script>
