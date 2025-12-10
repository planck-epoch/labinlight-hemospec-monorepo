import datetime
import csv

from . import iscpy as isc
from SDK import device


class Scan:
    def __init__(self):
        self.device = device.Device()
        self.errMessage = ""
        self.startScanTime = datetime.datetime.now()
        self.endScanTime = datetime.datetime.now()
        self.isReadBinFile = False
        self.WaveLength = []
        self.Intensity = []
        self.Absorbance = []
        self.Reflectance = []
        self.scanDataLen = 0
        self.scanPGA = 0
        self.scanConfig = ()
        self.scanDateTime = []
        self.scanSensor = []
        self.ReferenceIntensity = []
        self.RefScanConfig = ()
        self.RefScanPGA = 0
        self.RefScanDateTime = []
        self.RefScanSensor = []
        self.activated = 0

    def SetLamp(self, control):
        ret = -1
        if not isinstance(control, int) or control < 0 or control > 2:
            return ret
        isc.SetLamp(control)
        ret = 0
        return ret

    def SetLampDelay(self, time):
        ret = -1
        if not isinstance(time, int):
            return ret
        ret = isc.SetLampDelay(time)
        return ret

    def GetEstimatedScanTime(self):
        result = isc.GetEstimatedScanTime()
        return result

    def SetScanNumRepeats(self, repeat):
        ret = -1
        if not isinstance(repeat, int):
            return ret
        result = isc.SetScanNumRepeats(repeat)
        return result

    def SetFixedPGAGain(self, isFixed, gainVal):
        ret = -1
        if not (isinstance(isFixed, bool) and isinstance(gainVal, int)):
            return ret
        ret = isc.SetFixedPGAGain(isFixed, gainVal)
        return ret

    def SetPGAGain(self, gainVal):
        ret = -1
        if not isinstance(gainVal, int):
            return ret
        try:
            ret = isc.SetPGAGain(gainVal)
        except:
            print(ret)
        return ret

    def GetPGAGain(self):
        ret = isc.GetPGAGain()
        return ret

    def EnableGlitchFilter(self, enable):
        ret = -1
        if not isinstance(enable, bool):
            return ret
        ret = isc.EnableGlitchFilter(enable)
        return ret

    def ReadGlitchFilterStatus(self, enable):
        ret = -1
        if not isinstance(enable, bool):
            return ret
        ret = isc.ReadGlitchFilterStatus(enable)
        return ret

    def PerformScan(self, refSelect):
        ret = -1
        if not isinstance(refSelect, int) or refSelect < 0 or refSelect > 2:
            return ret
        self.startScanTime = datetime.datetime.now()
        ret = isc.PerformScan(refSelect)
        self.endScanTime = datetime.datetime.now()
        return ret

    def IsLocalReferenceExist(self):
        ret = isc.IsLocalReferenceExist()
        return ret

    # region Get Scan Result APIs

    def GetScanConfig(self):
        """
        0: scan type
        1: serial number
        2: config name
        3: num scans to average
        4: num section
        5: section list [section scan type, start nm, end nm, width px, exposure time, resolutions]
        """
        ret = -1
        self.scanConfig = ()
        result = isc.GetScanConfig()
        if not isinstance(result, tuple):
            return ret
        ret = 0
        self.scanConfig = result
        return ret

    def GetScanSensorData(self):
        """
        0: system temp
        1: detector temp
        2: humidity
        3: lamp indicator
        """
        ret = -1
        self.scanSensor = []
        result = isc.GetScanSensorData()
        if not isinstance(result, tuple):
            return ret
        ret = 0
        self.scanSensor = result
        return ret

    def GetScanDateTime(self):
        """
        0: year
        1: month
        2: day
        3: hour
        4: minute
        5: second
        """
        ret = -1
        self.scanDateTime = []
        result = isc.GetScanDateTime()
        if not isinstance(result, tuple):
            return ret
        ret = 0
        self.scanDateTime = result
        return ret

    def GetRefScanConfig(self):
        """
        0: scan type
        1: serial number
        2: config name
        3: num scans to average
        4: num section
        5: section list [section scan type, start nm, end nm, width px, exposure time, resolutions]
        """
        ret = -1
        self.RefScanConfig = ()
        result = isc.GetRefScanConfig()
        if not isinstance(result, tuple):
            return ret
        ret = 0
        self.RefScanConfig = result
        return ret

    def GetRefScanDateTime(self):
        """
        0: year
        1: month
        2: day
        3: hour
        4: minute
        5: second
        """
        ret = -1
        self.RefScanDateTime = []
        result = isc.GetRefScanDateTime()
        if not isinstance(result, tuple):
            return ret
        ret = 0
        self.RefScanDateTime = result
        return ret

    def GetRefScanSensorData(self):
        """
        0: system temp
        1: detector temp
        2: humidity
        3: lamp indicator
        """
        ret = -1
        self.RefScanSensor = []
        result = isc.GetRefScanSensorData()
        if not isinstance(result, tuple):
            return ret
        ret = 0
        self.RefScanSensor = result
        return ret

    def GetScanResult(self):
        self.activated = self.device.GetActivationResult()
        ret = self.device.Information()
        if ret < 0:
            return ret
        # Scan Configuration Information
        ret = self.GetScanConfig()
        if ret < 0:
            return ret
        ret = self.GetScanSensorData()
        if ret < 0:
            return ret
        ret = self.GetScanDateTime()
        if ret < 0:
            return ret
        self.scanPGA = isc.GetScanPGAGain()
        # Reference Scan Information
        ret = self.GetRefScanConfig()
        if ret < 0:
            return ret
        ret = self.GetRefScanSensorData()
        if ret < 0:
            return ret
        ret = self.GetRefScanDateTime()
        if ret < 0:
            return ret
        self.RefScanPGA = isc.GetRefScanPGAGain()
        # Others
        ret = self.device.GetCalibStruct()
        if ret < 0:
            return ret
        ret = self.device.ReadLampUsage()
        ret = self.device.ReadDeviceStatus()
        ret = self.device.ReadErrorStatus()
        # Scan Data
        self.scanDataLen = isc.GetScanDataLength()
        self.WaveLength = isc.GetWavelengths()
        self.Intensity = isc.GetIntensities()
        self.Absorbance = isc.GetAbsorbance()
        self.Reflectance = isc.GetReflectance()
        self.ReferenceIntensity = isc.GetRefIntensities()
        ret = 0
        return ret

    # endregion Get Scan Result APIs

    # region Save Scan Result APIs

    def SaveReferenceScan(self):
        ret = isc.SaveReferenceScan()
        return ret

    def SaveScanResultToBinFile(self, filename):
        ret = -1
        if not isinstance(filename, str):
            return ret
        ret = isc.SaveScanResultToBinFile(filename)
        return ret

    def ReadScanResultFromBinFile(self, filename):
        ret = -1
        if not isinstance(filename, str):
            return ret
        ret = isc.ReadScanResultFromBinFile(filename)
        return ret

    def GetScanType(self, index):
        match index:
            case 0:
                return "Column"
            case 1:
                return "Hadamard"
            case 2:
                return "Slew"

    def GetExposureTime(self, index):
        match index:
            case 0:
                return "0.635"
            case 1:
                return "1.27"
            case 2:
                return "2.54"
            case 3:
                return "5.08"
            case 4:
                return "15.24"
            case 5:
                return "30.48"
            case 6:
                return "60.96"
            case _:
                return "0.635"

    def SaveScanResultToCsvFile(self, filename, appname, version):
        ret = -1
        if not isinstance(filename, str) or not isinstance(appname, str) or not isinstance(version, str):
            return ret

        rows, cols = (28, 15)
        header = [["" for i in range(cols)] for j in range(rows)]

        # Config field names & values(Scan configuration and Reference scan configuration)
        header[0][0] = "***Scan Config Information***"
        header[0][7] = "***Reference Scan Information***"
        for i in range(0, 2):
            header[1][i * 7] = "Scan Config Name:"
            header[2][i * 7] = "Scan Config Type:"
            header[2][i * 7 + 2] = "Num Section:"
            header[3][i * 7] = "Section Config Type:"
            header[4][i * 7] = "Start Wavelength (nm):"
            header[5][i * 7] = "End Wavelength (nm):"
            header[6][i * 7] = "Pattern Width (nm):"
            header[7][i * 7] = "Exposure (ms):"
            header[8][i * 7] = "Digital Resolution:"
            header[9][i * 7] = "Num Repeats:"
            header[10][i * 7] = "PGA Gain:"
            header[11][i * 7] = "System Temp (C):"
            header[12][i * 7] = "Humidity (%):"
            header[13][i * 7] = "Lamp Indicator:"
            header[14][i * 7] = "Data Date-Time:"
        header[1][1] = self.scanConfig[2]
        header[2][1] = self.GetScanType(self.scanConfig[0])
        header[2][3] = str(self.scanConfig[4])
        sections = self.scanConfig[5]
        for i in range(0, len(sections)):
            header[3][1 + i] = self.GetScanType(sections[i][0])
            header[4][1 + i] = str(sections[i][1])
            header[5][1 + i] = str(sections[i][2])
            header[6][1 + i] = str(sections[i][3])
            header[7][1 + i] = self.GetExposureTime(sections[i][4])
            header[8][1 + i] = str(sections[i][5])
        header[9][1] = str(self.scanConfig[3])
        header[10][1] = str(self.scanPGA)
        header[11][1] = str(self.scanSensor[0])
        header[12][1] = str(self.scanSensor[2])
        header[13][1] = str(self.scanSensor[3])
        header[14][1] = f"{self.scanDateTime[0]:04d}/{self.scanDateTime[1]:02d}/{self.scanDateTime[2]:02d}" \
                        f"T{self.scanDateTime[3]:02d}:{self.scanDateTime[4]:02d}:{self.scanDateTime[5]:02d}"
        if self.RefScanConfig[2] == "SystemTest":
            refConfigName = "Built-in Factory Reference"
        elif self.RefScanConfig[2] == "UserReference":
            refConfigName = "Built-in User Reference"
        else:
            refConfigName = "Local New Reference"
        header[1][8] = refConfigName
        header[2][8] = self.GetScanType(self.RefScanConfig[0])
        header[2][10] = str(self.RefScanConfig[4])
        sections = self.RefScanConfig[5]
        for i in range(0, len(sections)):
            header[3][8 + i] = self.GetScanType(sections[i][0])
            header[4][8 + i] = str(sections[i][1])
            header[5][8 + i] = str(sections[i][2])
            header[6][8 + i] = str(sections[i][3])
            header[7][8 + i] = self.GetExposureTime(sections[i][4])
            header[8][8 + i] = str(sections[i][5])
        header[9][8] = str(self.RefScanConfig[3])
        header[10][8] = str(self.RefScanPGA)
        header[11][8] = str(self.RefScanSensor[0])
        header[12][8] = str(self.RefScanSensor[2])
        header[13][8] = str(self.RefScanSensor[3])
        header[14][8] = f"{self.RefScanDateTime[0]:04d}/{self.RefScanDateTime[1]:02d}/{self.RefScanDateTime[2]:02d}" \
                        f"T{self.RefScanDateTime[3]:02d}:{self.RefScanDateTime[4]:02d}:{self.RefScanDateTime[5]:02d}"

        # Measure time field name & value
        header[15][0] = "Total Measurement Time in sec:"
        header[15][1] = str((self.endScanTime - self.startScanTime).total_seconds())

        # General information field names & values
        header[17][0] = "***General Information***"
        header[18][0] = "Model Name:"
        header[18][1] = self.device.DevInfo['ModelName']
        header[19][0] = "Serial Number:"
        header[19][1] = self.device.DevInfo['SerialNumber']
        header[19][2] = "(" + self.device.DevInfo['Manufacturing_SerialNumber'] + ")"
        header[20][0] = "GUI Version:"
        header[20][1] = version
        header[20][2] = "(" + appname + ")"
        header[21][0] = "TIVA Version:"
        header[21][1] = self.device.DevInfo['TivaRev']
        header[22][0] = "DLPC Version:"
        header[22][1] = self.device.DevInfo['DLPCRev']
        header[23][0] = "UUID:"
        header[23][1] = self.device.DevInfo['DeviceUUID']
        header[24][0] = "Main Board Version:"
        header[24][1] = str(self.device.DevInfo['HWRev'][0])
        header[25][0] = "Detector Board Version:"
        header[25][1] = str(self.device.DevInfo['HWRev'][4])

        # Coefficients field names & values
        header[17][7] = "***Calibration Coefficients***"
        header[18][7] = "Shift Vector Coefficients:"
        header[18][8] = str(self.device.ShiftVectorCoeffs[0])
        header[18][9] = str(self.device.ShiftVectorCoeffs[1])
        header[18][10] = str(self.device.ShiftVectorCoeffs[2])
        header[19][7] = "Pixel to Wavelength Coefficients:"
        header[19][8] = str(self.device.PixelToWavelengthCoeffs[0])
        header[19][9] = str(self.device.PixelToWavelengthCoeffs[1])
        header[19][10] = str(self.device.PixelToWavelengthCoeffs[2])
        header[20][7] = "Versions (Cal/Ref/Cfg):"
        header[20][8] = str(self.device.DevInfo['CalRev'])
        header[20][9] = str(self.device.DevInfo['RefCalRev'])
        header[20][10] = str(self.device.DevInfo['CfgRev'])

        # Others field names & values
        header[21][7] = "***Lamp Usage ***"
        header[22][7] = "Total Time:"
        header[22][8] = str(datetime.timedelta(milliseconds=self.device.lampUsage)) if self.activated else "N/A"
        header[23][7] = "***Device/Error/Activation Status***"
        header[24][7] = "Device Status:"
        header[24][8] = str(self.device.deviceStatus)
        header[24][9] = "Activation Status:"
        header[24][10] = "Activated" if self.activated else "Not activated"
        header[25][7] = "Error Status:"
        header[25][8] = str(self.device.errStatus)
        header[25][9] = "Error Code:"
        header[25][10] = str(self.device.errCode)
        header[26][9] = "Error Details:"
        # header[26][10]
        header[27][0] = "***Scan Data***"

        # Scan data
        data = ["Wavelength (nm)", "Absorbance (AU)", "Reference Signal (unitless)", "Sample Signal (unitless)"]
        scan_data = [data]
        for i in range(0, self.scanDataLen):
            data = [str(self.WaveLength[i]), str(self.Absorbance[i]),
                    str(self.ReferenceIntensity[i]), str(self.Intensity[i])]
            scan_data.append(data)

        # Write to csv file
        with open(filename, 'w', encoding='UTF8', newline='') as f:
            writer = csv.writer(f)
            writer.writerows(header)
            writer.writerows(scan_data)
        ret = 0
        return ret

    # endregion Save Scan Result APIs
