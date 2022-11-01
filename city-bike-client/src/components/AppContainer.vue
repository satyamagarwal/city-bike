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
  <div class="osg-container">
    <ShowList
      :stations-and-statuses="searchedStationsAndStatuses"
      :search-param="searchTerm"
    />
  </div>
</template>

<script>
import ShowList from "./ShowList.vue";
import PageHeader from "./PageHeader.vue";
import {config} from 'icefog'

const host = config.isProd ? '' : 'http://localhost:8080'

export default {
  name: "AppContainer",
  components: {
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
