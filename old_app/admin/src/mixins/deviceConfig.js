export default {
  methods: {
    $parseScanType: function (value) {
      switch (value) {
        case 0:
          return "Column";
        case 1:
          return "Hadamard";
      }
      return "";
    },
    $parseExposureTime: function (value) {
      switch (value) {
        case 0:
          return 0.635;
        case 1:
          return 1.27;
        case 2:
          return 2.54;
        case 3:
          return 5.08;
        case 4:
          return 15.24;
        case 5:
          return 30.48;
        case 6:
          return 60.96;
      }
      return "";
    },
    $parseWidth: function (value) {
      switch (value) {
        case 2:
          return 2.34;
        case 3:
          return 3.51;
        case 4:
          return 4.68;
        case 5:
          return 5.85;
        case 6:
          return 7.03;
        case 7:
          return 8.2;
        case 8:
          return 9.37;
        case 9:
          return 10.54;
        case 10:
          return 11.71;
        case 11:
          return 12.88;
        case 12:
          return 14.05;
        case 13:
          return 15.22;
        case 14:
          return 16.39;
        case 15:
          return 17.56;
        case 16:
          return 18.74;
        case 17:
          return 19.91;
        case 18:
          return 21.08;
        case 19:
          return 22.25;
        case 20:
          return 23.42;
        case 21:
          return 24.59;
        case 22:
          return 25.76;
        case 23:
          return 26.93;
        case 24:
          return 28.1;
        case 25:
          return 29.27;
        case 26:
          return 30.44;
        case 27:
          return 31.62;
        case 28:
          return 32.79;
        case 29:
          return 33.96;
        case 30:
          return 35.13;
        case 31:
          return 36.3;
        case 32:
          return 37.47;
        case 33:
          return 38.64;
        case 34:
          return 39.81;
        case 35:
          return 40.98;
        case 36:
          return 42.15;
        case 37:
          return 43.33;
        case 38:
          return 44.5;
        case 39:
          return 45.67;
        case 40:
          return 46.84;
        case 41:
          return 48.01;
        case 42:
          return 49.18;
        case 43:
          return 50.35;
        case 44:
          return 51.52;
        case 45:
          return 52.69;
        case 46:
          return 53.86;
        case 47:
          return 55.04;
        case 48:
          return 56.21;
        case 49:
          return 57.38;
        case 50:
          return 58.55;
        case 51:
          return 59.72;
        case 52:
          return 60.89;
      }
      return "";
    },
  },
};
