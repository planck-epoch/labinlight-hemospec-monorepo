/*
 * Device and error status definitions
 *
 * Copyright (C) 2018 InnoSpectra Corp. - http://www.inno-spectra.com/
 * ALL RIGHTS RESERVED
 *
 */

#ifndef ISCSTATUSDEFS_H_
#define ISCSTATUSDEFS_H_

#define MAX_NUM_DEVICE_STATUS 32
#define MAX_NUM_ERROR_STATUS  32

/************ 		Device Status Definitions ***************/
// TIVA status = 0 - ERROR, 1 - NORMAL
#define ISC_STATUS_TIVA                             0x00000001
#define ISC_STATUS_SCAN_IN_PROGRESS                 0x00000002
#define ISC_STATUS_SD_CARD_PRESENT                  0x00000004
#define ISC_STATUS_SD_CARD_OPER_IN_PROG             0x00000008
#define ISC_STATUS_BLE_STACK_OPEN                   0x00000010
#define ISC_STATUS_ACTIVE_BLE_CONNECTION            0x00000020
#define ISC_STATUS_SCAN_INTERPRET_IN_PROGRESS       0x00000040
#define ISC_STATUS_SCAN_BUTTON_PRESSED              0x00000080
#define ISC_STATUS_BATTERY_IN_CHARGE                0x00000100
#define ISC_STATUS_MAX                              ISC_STATUS_BATTERY_IN_CHARGE

typedef enum
{
    ISC_device_status_tiva,
    ISC_device_status_scan_in_progress,
    ISC_device_status_ble_stack,
    ISC_device_status_ble_active,
    ISC_device_status_scan_interpreting,
    ISC_device_status_scan_button_pressed,
    ISC_device_status_battery_in_charge,
    ISC_device_status_max
} ISC_device_status_type;        // the order of enums should match the error code order above

/************ 		Error Status Defintions *****************/
#define ISC_ERROR_SCAN                              0x00000001
#define ISC_ERROR_ADC                               0x00000002
#define ISC_ERROR_SD_CARD                           0x00000004
#define ISC_ERROR_EEPROM                            0x00000008
#define ISC_ERROR_BLE                               0x00000010
#define ISC_ERROR_SPEC_LIB                          0x00000020
#define ISC_ERROR_HW                                0x00000040
#define ISC_ERROR_TMP006                            0x00000080
#define ISC_ERROR_HDC1000                           0x00000100
#define ISC_ERROR_BATTERY_EMPTY                     0x00000200
#define ISC_ERROR_INSUFFICIENT_MEMORY               0x00000400
#define ISC_ERROR_UART                              0x00000800
#define ISC_ERROR_SYSTEM                            0x00001000
#define ISC_ERROR_MAX                               ISC_ERROR_SYSTEM

typedef enum
{
    ISC_error_code_scan,
    ISC_error_code_adc,
    ISC_error_code_sd, //Deprecated
    ISC_error_code_eeprom,
    ISC_error_code_ble,
    ISC_error_code_spec_lib,
    ISC_error_code_hw,
    ISC_error_code_tmp, //TMP006 - deprecated
    ISC_error_code_hdc, //HDC1000
    ISC_error_code_battery, //Model dependency
    ISC_error_code_memory,
    ISC_error_code_uart,
    ISC_error_code_system,
    ISC_error_code_max
} ISC_error_codes_type;        // the order of enums should match the error code order above

/******************** 	Scan Error codes   **********************/
#define ISC_ERROR_SCAN_DLPC150_BOOT_ERROR           0x00000001
#define ISC_ERROR_SCAN_DLPC150_INIT_ERROR           0x00000002
#define ISC_ERROR_SCAN_DLPC150_LAMP_DRIVER_ERROR    0x00000004
#define ISC_ERROR_SCAN_DLPC150_CROP_IMG_FAILED      0x00000008
#define ISC_ERROR_SCAN_ADC_DATA_ERROR               0x00000010
#define ISC_ERROR_SCAN_CFG_INVALID                  0x00000020
#define ISC_ERROR_SCAN_PATTERN_STREAMING            0x00000040
#define ISC_ERROR_SCAN_DLPC150_READ_ERROR           0x00000080

/******************** 	ADC Error codes   ***********************/
#define ISC_ERROR_ADC_START                         0x00000001
#define ISC_ADC_ERROR_TIMEOUT                       ISC_ERROR_ADC_START
#define ISC_ADC_ERROR_POWERDOWN                     (ISC_ERROR_ADC_START + 1)
#define ISC_ADC_ERROR_POWERUP                       (ISC_ERROR_ADC_START + 2)
#define ISC_ADC_ERROR_STANDBY                       (ISC_ERROR_ADC_START + 3)
#define ISC_ADC_ERROR_WAKEUP                        (ISC_ERROR_ADC_START + 4)
#define ISC_ADC_ERROR_READREGISTER                  (ISC_ERROR_ADC_START + 5)
#define ISC_ADC_ERROR_WRITEREGISTER                 (ISC_ERROR_ADC_START + 6)
#define ISC_ADC_ERROR_CONFIGURE                     (ISC_ERROR_ADC_START + 7)
#define ISC_ADC_ERROR_SETBUFFER                     (ISC_ERROR_ADC_START + 8)
#define ISC_ADC_ERROR_COMMAND                       (ISC_ERROR_ADC_START + 9)
#define ISC_ADC_ERROR_SET_PGA                       (ISC_ERROR_ADC_START + 10)

/********************     BLE Error codes   ***********************/
#ifndef ccs
    #define BLE_OPEN_STACK_FAILED                        -4
    #define APPLICATION_TASK_CREATION_FAILED            (-1001)
    #define APPLICATION_ERROR_NO_COMMAND                (-1002)
    #define APPLICATION_ERROR_INVALID_COMMAND           (-1003)
    #define APPLICATION_ERROR_EXIT_CODE                 (-1004)
    #define APPLICATION_ERROR_FUNCTION                  (-1005)
    #define APPLICATION_ERROR_TOO_MANY_PARAMS           (-1006)
    #define APPLICATION_ERROR_INVALID_PARAMETERS        (-1007)
    #define APPLICATION_ERROR_UNABLE_TO_OPEN_STACK      (-1008)
    #define APPLICATION_ERROR_INVALID_STACK_ID          (-1009)
    #define APPLICATION_ERROR_GATT_SERVICE_EXISTS       (-1010)
    #define APPLICATION_ERROR_GAPS                      (-1011)
#endif

/********************   SPECLIB Error codes   ***********************/
#ifndef ccs
    #define ERR_DLPSPEC_FAIL                            -1
    #define ERR_DLPSPEC_INVALID_INPUT                   -2
    #define ERR_DLPSPEC_INSUFFICIENT_MEM                -3
    #define ERR_DLPSPEC_TPL                             -4
    #define ERR_DLPSPEC_ILLEGAL_SCAN_TYPE               -5
    #define ERR_DLPSPEC_NULL_POINTER                    -6
#endif

/********************* 	HW Error codes   ************************/
#define ISC_ERROR_HW_START                          0x00000001
#define ISC_ERROR_HW_DLPC150                        (ISC_ERROR_HW_START)
#define ISC_ERROR_HW_UUID                           (ISC_ERROR_HW_START + 1)
#define ISC_ERROR_HW_FLASH_INIT                     (ISC_ERROR_HW_START + 2)
#define ISC_ERROR_HW_MAX                            (ISC_ERROR_HW_START + 3) // Modify this entry when new codes are added above

/********************   TMP006 Error codes   ********************/
#define ISC_ERROR_TMP006_START                      0x00000001
#define ISC_ERROR_TMP006_MANUID                     (ISC_ERROR_TMP006_START)
#define ISC_ERROR_TMP006_DEVID                      (ISC_ERROR_TMP006_START + 1)
#define ISC_ERROR_TMP006_RESET                      (ISC_ERROR_TMP006_START + 2)
#define ISC_ERROR_TMP006_READREGISTER               (ISC_ERROR_TMP006_START + 3)
#define ISC_ERROR_TMP006_WRITEREGISTER              (ISC_ERROR_TMP006_START + 4)
#define ISC_ERROR_TMP006_TIMEOUT                    (ISC_ERROR_TMP006_START + 5)
#define ISC_ERROR_TMP006_I2C                        (ISC_ERROR_TMP006_START + 6)
#define ISC_ERROR_TMP006_MAX                        (ISC_ERROR_TMP006_START + 7) // Modify this entry when new codes are added above

/******************** 	HDC1000 Error codes   *******************/
#define ISC_ERROR_HDC1000_START                     0x00000001
#define ISC_ERROR_HDC1000_MANUID                    (ISC_ERROR_HDC1000_START)
#define ISC_ERROR_HDC1000_DEVID                     (ISC_ERROR_HDC1000_START + 1)
#define ISC_ERROR_HDC1000_RESET                     (ISC_ERROR_HDC1000_START + 2)
#define ISC_ERROR_HDC1000_READREGISTER              (ISC_ERROR_HDC1000_START + 3)
#define ISC_ERROR_HDC1000_WRITEREGISTER             (ISC_ERROR_HDC1000_START + 4)
#define ISC_ERROR_HDC1000_TIMEOUT                   (ISC_ERROR_HDC1000_START + 5)
#define ISC_ERROR_HDC1000_I2C                       (ISC_ERROR_HDC1000_START + 6)
#define ISC_ERROR_HDC1000_MAX                       (ISC_ERROR_HDC1000_START + 7) // Modify this entry when new codes are added above

/********************   Battery Error codes   *******************/
#define ISC_ERROR_BATTERY_START                     0x00000001
#define ISC_ERROR_BATTERY_UNDER_VOL                 (ISC_ERROR_BATTERY_START)
#define ISC_ERROR_BATTERY_MAX                       (ISC_ERROR_BATTERY_START + 1) // Modify this entry when new codes are added above

/********************   UART Error codes   *******************/
#ifndef ISCUARTDEFS_H_ //These are also defined in ISCUARTDefs.h
    #define UART_INCOMP_START_END_IND_RECD   -1
    #define UART_INPUT_PKT_CHECKSUM_ERROR    -2
    #define UART_WRITE_FAILED                -3
#endif

/********************   System Error codes   *******************/
#define ISC_ERROR_SYSTEM_START                      0x00000001
#define ISC_ERROR_SYSTEM_UNSTABLE_LAMP_ADC          (ISC_ERROR_SYSTEM_START)
#define ISC_ERROR_SYSTEM_UNSTABLE_PEAK_INTENSETY    (ISC_ERROR_SYSTEM_START << 1)
#define ISC_ERROR_SYSTEM_ADS1255                    (ISC_ERROR_SYSTEM_START << 2)
#define ISC_ERROR_SYSTEM_AUTOPGA                    (ISC_ERROR_SYSTEM_START << 3)
#define ISC_ERROR_SYSTEM_UNSTABLE_SCAN_IN_REPEATED  (ISC_ERROR_SYSTEM_START << 4)

#define MAX_BLE_PKT_SIZE 20
#define RESERVED_SIZE    (MAX_BLE_PKT_SIZE - 4 - ISC_error_code_max - 2)	/* -2 because ble and system each use two bytes */

typedef struct
{
    int8_t scan;
    int8_t adc;
    int8_t sd;
    int8_t eeprom;
    int16_t ble;
    int8_t spec_lib;
    int8_t hw;
    int8_t tmp;
    int8_t hdc;
    int8_t battery;
    int8_t memory;
    int8_t uart;
    int16_t system;
    int8_t reserved[RESERVED_SIZE];	// Future use
} ISC_error_codes_struct;

typedef struct
{
    uint32_t status;
    ISC_error_codes_struct errorCodes;
} ISC_error_status_struct; // Size of struct should not exceed MTU size for BLE

typedef struct
{
    uint32_t deviceStatus;
    ISC_error_status_struct errorStatus;
} ISC_status_struct;

#endif /* ISCSTATUSDEFS_H_ */
