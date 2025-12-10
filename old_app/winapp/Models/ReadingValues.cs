using System;
using System.Collections.Generic;
using System.Text;

namespace LabinLightApi.Models
{
    public class ReadingValues
    {
        public DeviceConfigLog DeviceConfigLog { get; set; }
        public string AnalysisBundle { get; set; }
        public string ProcessNumber { get; set; }
        public string Sex { get; set; }
        public int BirthdayYear { get; set; }
        public int Age { get; set; }
        public string PatientIdCountry { get; set; }
        public string PatientId { get; set; }
        public string PatientContactNumber { get; set; }
        public string ModelName { get; set; }
        public string SerialNumber { get; set; }
        public DateTime DateReading { get; }
        public List<Sample> Samples { get; }
        private string uuid { get; }

        public ReadingValues() { 
            uuid = Guid.NewGuid().ToString();
            Samples = new List<Sample>();
            DateReading = DateTime.Now;

        }


    }

    public class Sample
    {

        public double TotalTimeScan { get; set; } 
      //  public long TimeScanStart { get; set; }
       // public long TimeScanEnd { get; set; }

        //DEVICE IDENTIFICATION
        public byte[] UUID { get; set; } 
        public byte[] HostDateTime { get; set; }

        // SCAN SENSORS INFO
        public double SystemTemp { get; set; }     
        public double DetectorTemp { get; set; } 
        public double Humidity { get; set; }
        public double LampPD { get; set; } 
        public double[] ShiftVectorCoefficients { get; set; } 
        public double[] PixelWavelengtCoefficients { get; set; } 

        // SCAN INFO
        public int HeaderVersion { get; set; }
        public string Method { get; set; }
        public string ScanConfigName { get; set; } 
        public byte ScanConfigType { get; set; } 
        public byte NumSection { get; set; } 
        public int StartWavelength { get; set; } 
        public int EndWavelength { get; set; } 
        public double PatternPixelWidth { get; set; } 
        public double Exposure { get; set; }
        public int DigitalResolution { get; set; } 
        public int NumRep { get; set; }
        public byte PGAgain { get; set; }

        //SCAN DATA
        public List<double> Absorbance { get; set; }
        public List<double> WaveLength { get; set; }
        //public List<double> Reflectance { get; set; }
        public List<int> Intensity { get; set; }

        //reference device
        public string RefSerialNumber { get; set; } 
        public byte[] RefDateTime { get; set; }
        //reference scan
        //public string RefScanConfigName { get; set; }
        public byte ReferencePGA { get; set; }
        public List<int> ReferenceIntensity { get; set; }
        public int ReferenceScanDataVersion { get;  set; }
        public byte RefScanConfigType { get; set; } 
        public SlewScanConfig ReferenceScanConfigData { get; set; }

        //reference sensors info
        public double RefTemp { get; set; }
        public double RefDetectorTemp { get; set; }
        public double RefHumidity { get; set; }
        public double RefLampPD { get; set; }

        #region Other decalrations TO DELETE
        //  //Scan Config Name + Reference Scan Config Name
        ////  RefScanConfigName = Scan.ReferenceScanConfigData.head.config_name
        //  //Section
        //  public byte RefNumSection { get; set; } // 
        //  //Start wavelength(nm) + Refence
        //  public ushort RefStartWavelength { get; set; }
        //  //End wavelength(nm) + Refence
        //  public ushort RefSEndWavelength { get; set; }
        //  //Pattern Pixel Width(nm)  + Refence
        // public byte PatternPixelWidth { get; set; } //
        //  public byte RefPatternPixelWidth { get; set; } //
        //  //Exposure (ms)
        //  public ushort RefExposureh { get; set; }//
        //  //Digital Resolution
        //  public ushort RefDigitalResolution { get; set; }//
        //  //Total Scan Ptns
        //  public ushort TotalScanPtns { get; set; }//
        //  public ushort TotalRefPtns { get; set; }//
        //// sensor data var
        //public double SensorDataTemp { get; set; }
        ////Reference Sensor
        //public double[] ReferenceSensorData { get; internal set; }

        #endregion



    }

 

}



