<template>
  <nav
    class="osg-pagination"
    :aria-label="ariaLabel"
  >
    <button
      v-show="showArrows && currentIndex > 1"
      class="osg-pagination__previous"
      @click.prevent="paginate(currentIndex - 1)"
    >
      <span class="osg-sr-only">{{ i18n.previousBtn }} {{ currentIndex - 1 }}</span>
    </button>
    <template
      v-for="index in totalPages"
      :key="index"
    >
      <button
        v-if="showItem(index)"
        :key="index"
        class="osg-pagination__item"
        :class="{ 'osg-pagination__item--current': index === currentIndex, 'osg-pagination__item--rectangle': index >= 100 }"
        :disabled="index === currentIndex && disableCurrentIndexBtn"
        :data-testid="[index === currentIndex ? 'current-index' : `index-${index}`]"
        @click.prevent="paginate(index)"
      >
        <span class="osg-sr-only">{{ i18n.showPage }} {{ index }}</span>
        {{ index }}
      </button>
      <span
        v-else
        class="osg-pagination__spacer"
        aria-hidden="true"
      >&hellip;</span>
    </template>
    <button
      v-show="showArrows && currentIndex < totalPages"
      class="osg-pagination__next"
      @click.prevent="paginate(currentIndex + 1)"
    >
      <span class="osg-sr-only">{{ i18n.nextBtn }} {{ currentIndex + 1 }}</span>
    </button>
  </nav>
</template>

<script>
export default {
  name: "OsgPagination",
  props: {
    ariaLabel: {
      type: String,
      required: true,
    },
    totalPages: {
      type: Number,
      required: true,
    },
    currentIndex: {
      type: Number,
      required: true,
    },
    disableCurrentIndexBtn: {
      type: Boolean,
      default: true,
    },
    showArrows: {
      type: Boolean,
      default: true,
    },
    threshold: {
      type: Number,
      default: 10,
    },
    limit: {
      type: Number,
      default: 2,
    },
    i18n: {
      type: Object,
      default: () => {
        return {
          previousBtn: "Show previous page",
          nextBtn: "Show next page",
          showPage: "Show page",
        };
      },
    },
  },
  emits: ['paginate'],
  computed: {
    limitMax: function () {
      return this.currentIndex + this.limit;
    },
    limitMin: function () {
      return this.currentIndex - this.limit;
    },
  },
  methods: {
    showItem: function (index) {
      const isFirst = index === 1;
      const isLast = index === this.totalPages;
      const isWithinLimit = index >= this.limitMin && index <= this.limitMax;
      const threshold = this.totalPages <= this.threshold;

      return isFirst || isLast || isWithinLimit || threshold;
    },
    showSpacer: function (index) {
      return this.totalPages > this.threshold && index >= this.limitMin - 1 && index <= this.limitMax + 1;
    },
    paginate: function (value) {
      this.$emit("paginate", value);
    }
  },
};
</script>
