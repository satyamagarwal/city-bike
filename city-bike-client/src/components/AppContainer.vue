<template>
  <header class="osg-header">
    <div class="osg-header__logo">
      <figure class="osg-logo">
        <img src="./../assets/oslo_logo.svg"  alt="Oslo Kommune logo"/>
      </figure>
    </div>
    <div class="osg-header__content">
      <div class="osg-none osg-block-breakpoint-large osg-full-height">
        <div class="osg-grid osg-grid--gap osg-full-height">
          <div class="osg-grid__column--5">
            <div class="osg-full-height osg-flex osg-flex-justify-content-center osg-flex-align-items-center osg-text-size-golf">
              Oslo Bysykkle: Stasjon søk
            </div>
          </div>
          <div class="osg-grid__column--7">
            <div class="osg-full-height osg-flex osg-flex-justify-content-flex-end osg-flex-align-items-center">
              <input v-model="search" class="osg-input__input" type="text" autocomplete="on" aria-label="Label text" placeholder="Søk etter et adress" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
  <div class="osg-container">
    <ShowList :stationsAndStatuses="stationsAndStatuses" :searchParam="search" />
  </div>
</template>

<script>
import ShowList from "./ShowList.vue";
import { config } from 'icefog'

const host = config.isProd ? '' : 'http://localhost:8080'

export default {
  name: "AppContainer",
  components: {
    ShowList
  },
  data() {
    return {
      search: '',
      stationsAndStatuses: []
    }
  },
  methods: {
    async fetchInfoAndStatus() {
      const response = await fetch(`${host}/station-status`)

      if(response.ok) {
        const content = await response.json()
        this.stationsAndStatuses = content
            .sort((station1, station2) => station1.info.name.localeCompare(station2.info.name))
      } else {
        this.stationsAndStatuses = []
      }
    }
  },
  mounted() {
    this.fetchInfoAndStatus()

    setInterval(this.fetchInfoAndStatus,10000);
  }
}
</script>
