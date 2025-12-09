#pragma once
#include <stdint.h>
#include <malloc.h>
#include <stdlib.h>
#include <string.h>
#include "..\src\dlpspec_types.h"
#include "..\src\dlpspec_setup.h"
#include "..\src\dlpspec_util.h"
#include "..\src\dlpspec_calib.h"
#include "..\src\dlpspec.h"
#include "..\src\dlpspec_scan_col.h"
#include "..\src\dlpspec_scan_had.h"
#include "..\src\dlpspec_scan.h"
#define EXPORT_DLL extern "C" _declspec(dllexport)
EXPORT_DLL int DeserializeScanConfig(void* pBuf, size_t bufSize);
EXPORT_DLL int SerializeScanConfig(const slewScanConfig* cfg, void* pBuf, size_t bufSize);
EXPORT_DLL int GetScanConfigDumpSize(const slewScanConfig* cfg, size_t* bufSize);
EXPORT_DLL int ScanInterpret(void* pBuf, size_t bufSize,scanResults *scanresults);
EXPORT_DLL int RefScanInterpret(const void* pRefCal, size_t calSize, const void* pMatrix, size_t matrixSize, const scanResults* pScanResults, scanResults* pRefResults);
EXPORT_DLL int CfgWidthIndexToPixel(int Index);
EXPORT_DLL int ScanConfig_GetMaxResolutions(slewScanConfig* scanCfg, int section, char* SpectrumCalCoefficients,  int minWav, int maxWav);
EXPORT_DLL int ScanConfig_GetHadamardUsedPatterns(slewScanConfig* scanCfg, int section, char* SpectrumCalCoefficients);