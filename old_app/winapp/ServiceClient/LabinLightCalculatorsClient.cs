using LabinLightApi.Models;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Security.Authentication;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;

namespace LabinLightScan.ServiceClient
{
    public class LabinLightReadingsServiceClient
    {

        private readonly string BaseUrl = "https://lil-app-dev.datainlight.com";
        public static readonly String ServerName = "Dev";

        private readonly HttpClient _client;

        public static readonly LabinLightReadingsServiceClient instance = new LabinLightReadingsServiceClient();

        private LabinLightReadingsServiceClient()
        {

            var handler = new HttpClientHandler();

            handler.ClientCertificateOptions = ClientCertificateOption.Manual;
            handler.SslProtocols = SslProtocols.Tls12;
            handler.ClientCertificates.Add(new X509Certificate2("cert.pfx"));
            _client = new HttpClient(handler);

            if (File.Exists("dev.config"))
            {
                var data = new Dictionary<string, string>();
                foreach (var row in File.ReadAllLines("dev.config"))
                    data.Add(row.Split('=')[0], string.Join("=", row.Split('=').Skip(1).ToArray()));
                if (data.ContainsKey("server.url"))
                {
                    Console.WriteLine("Dev.cfg override found:" + data["server.url"]);
                    BaseUrl = data["server.url"];
                }
            }
        }

        
        public async Task<List<AnalysisType>> FetchAnalysisTypesAsync()
        {
            var httpResponse = await _client.GetAsync(BaseUrl + "/analysis_bundle");
            if (!httpResponse.IsSuccessStatusCode)
            {
                var errContent = await httpResponse.Content.ReadAsStringAsync();
                throw new Exception($"Failed to fetch analysis types: {httpResponse.StatusCode} - {errContent}");
            }
            var json = await httpResponse.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<List<AnalysisType>>(json);
        }


        public async Task<List<BloodResult>> CalcAsync(ReadingValues readingValues)
        {
            var content = JsonConvert.SerializeObject(readingValues);
            var httpResponse = await _client.PostAsync(BaseUrl + "/analyze", new StringContent(content, Encoding.UTF8, "application/json"));

            if (!httpResponse.IsSuccessStatusCode)
            {
                var errContent = httpResponse.Content.ReadAsStringAsync().Result;
                if(httpResponse.StatusCode == HttpStatusCode.InternalServerError && int.TryParse(errContent, out _))
                {
                    throw new KnownServiceErrorException("Erro " + errContent + ": A leitura foi considerada inválida, certifique-se que o cartucho se encontra corretamente inserido no Hemospec.");
                }
                throw new Exception(httpResponse.StatusCode.ToString() + " - " + errContent );
            }
            var createdTask = JsonConvert.DeserializeObject<List<BloodResult>>(await httpResponse.Content.ReadAsStringAsync());
            return createdTask;
        }

        public async Task CalibrateDeviceAsync(string serialNumber, Sample scanResult)
        {
            var content = JsonConvert.SerializeObject(scanResult);
            var httpResponse = await _client.PostAsync(BaseUrl + $"/device/{serialNumber}/calibrate", new StringContent(content, Encoding.UTF8, "application/json"));
            if (!httpResponse.IsSuccessStatusCode)
            {
                var errContent = await httpResponse.Content.ReadAsStringAsync();
                throw new Exception($"Calibration failed: {httpResponse.StatusCode} - {errContent}");
            }
        }

        public SlewScanConfig GetDeviceConfig(String deviceSerialNumber)
        {
            var httpResponse = _client.GetAsync(BaseUrl + "/device/" + deviceSerialNumber + "/config");
            httpResponse.Wait();

            var content = httpResponse.Result.Content.ReadAsStringAsync().Result;
            if (!httpResponse.Result.IsSuccessStatusCode)
            {
                if (httpResponse.Result.StatusCode == HttpStatusCode.NotFound)
                {
                    throw new KnownServiceErrorException("Erro a obter configuração de dispositivo: " + content + ", processo irá continuar com configuração predefinida no Hemospec.");
                }
                throw new Exception(httpResponse.Result.StatusCode.ToString() + " - " + content);
            }
            var config = new SlewScanConfig();
            JsonConvert.PopulateObject(content, config);
            return config;
        }

        public void SendDeviceConfigLogs(DeviceConfigLog log)
        {
            try
            {
                // FIXME: send logs to proper endpoint once it is actually implemented
                var content = JsonConvert.SerializeObject(log);
                var httpResponse = _client.PostAsync(BaseUrl + "/log", new StringContent(content, Encoding.UTF8, "application/json"));
                httpResponse.Wait();
            }
            catch { }
        }


        public bool CheckServerStatus()
        {
            var httpResponse = _client.GetAsync(BaseUrl + "/health");
            httpResponse.Wait();
            return httpResponse.Result.IsSuccessStatusCode;
        }

    }
}