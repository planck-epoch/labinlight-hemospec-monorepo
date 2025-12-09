#include "pch.h"
#include "DLPSpecLib.h"

#define CFG_WIDTH_PIXEL_MIN 2
uint16_t minWavelength;
uint16_t maxWavelength;
#define PASS    0
#define FAIL    -1
#define DMD_HEIGHT 480

int DeserializeScanConfig(void* pBuf, size_t bufSize)
{
    return dlpspec_scan_read_configuration(pBuf, bufSize);
}

int SerializeScanConfig(const slewScanConfig* cfg, void* pBuf, size_t bufSize)
{
    return dlpspec_scan_write_configuration((uScanConfig*)cfg, pBuf, bufSize);
}

int GetScanConfigDumpSize(const slewScanConfig* cfg, size_t* bufSize)
{
    return dlpspec_get_scan_config_dump_size((uScanConfig*)cfg, bufSize);
}

int ScanInterpret(void* pBuf, size_t bufSize, scanResults *finalScanResults)
{
    return  dlpspec_scan_interpret(pBuf, bufSize, finalScanResults);
}

int RefScanInterpret(const void* pRefCal, size_t calSize, const void* pMatrix, size_t matrixSize, const scanResults* pScanResults, scanResults* pRefResults)
{
    return dlpspec_scan_interpReference(pRefCal,calSize,pMatrix,matrixSize,pScanResults,pRefResults);
}
int CfgWidthIndexToPixel(int Index)
{
    return Index + CFG_WIDTH_PIXEL_MIN;
}
int ScanConfig_GetMaxResolutions(slewScanConfig* scanCfg, int section, char* SpectrumCalCoefficients, int minWav, int maxWav)
{
    int maxResolutions = 0;
    int maxShift = 0;   // maximum shift across all rows of the DMD
    int blockShift = 0; // maximum shift in a 4-row block
    int8_t* shiftVector = NULL;
    calibCoeffs m_calibCoeffs;
    DLPSPEC_ERR_CODE ret_val = DLPSPEC_PASS;
    minWavelength = minWav;
    maxWavelength = maxWav;

    scanConfig tmpCfg;
    strcpy_s(tmpCfg.config_name, scanCfg->head.config_name);
    tmpCfg.scan_type = scanCfg->section[section].section_scan_type;
    tmpCfg.wavelength_start_nm = scanCfg->section[section].wavelength_start_nm;
    tmpCfg.wavelength_end_nm = scanCfg->section[section].wavelength_end_nm;
    tmpCfg.width_px = scanCfg->section[section].width_px;
    tmpCfg.num_repeats = scanCfg->head.num_repeats;

    if (tmpCfg.wavelength_start_nm < minWavelength || tmpCfg.wavelength_end_nm > maxWavelength ||
        tmpCfg.wavelength_start_nm >= tmpCfg.wavelength_end_nm)
        return FAIL;

    char byteData[sizeof(calibCoeffs) * 3] = "";
    memcpy(byteData, SpectrumCalCoefficients, sizeof(calibCoeffs) * 3);
    dlpspec_calib_read_data(&byteData, sizeof(calibCoeffs) * 3);
    memcpy(&m_calibCoeffs, &byteData, sizeof(calibCoeffs));

    if (m_calibCoeffs.PixelToWavelengthCoeffs[0] > 2400) // EXT PLUS
    {
        m_calibCoeffs.PixelToWavelengthCoeffs[0] = 2484.902664; //2419.143;
        m_calibCoeffs.PixelToWavelengthCoeffs[1] = -0.874372; //-0.78287;
        m_calibCoeffs.PixelToWavelengthCoeffs[2] = -0.000278; //-0.00027;
    }
    else if (m_calibCoeffs.PixelToWavelengthCoeffs[0] > 2150) // EXT
    {
        m_calibCoeffs.PixelToWavelengthCoeffs[0] = 2234.902664; //2236.4595646231;
        m_calibCoeffs.PixelToWavelengthCoeffs[1] = -0.874372; //-0.8894845388;
        m_calibCoeffs.PixelToWavelengthCoeffs[2] = -0.000278; //-0.0002346826;
    }
    else // STD
    {
        m_calibCoeffs.PixelToWavelengthCoeffs[0] = 1784.902664;
        m_calibCoeffs.PixelToWavelengthCoeffs[1] = -0.874372;
        m_calibCoeffs.PixelToWavelengthCoeffs[2] = -0.000278;
    }
    /*
        The following shift factors are used on current EXT and PLUS models.
        To be getting the same maximum resolution for all ISC devices, use STD's for calculation.

        m_calibCoeffs.ShiftVectorCoeffs[0] = -7.8536993885;
        m_calibCoeffs.ShiftVectorCoeffs[1] = 0.0719566491;
        m_calibCoeffs.ShiftVectorCoeffs[2] = -0.0001634704;
    */
    // STD's shift factors for all
    m_calibCoeffs.ShiftVectorCoeffs[0] = -5.81006908046050000;
    m_calibCoeffs.ShiftVectorCoeffs[1] = 0.04703071690475610;
    m_calibCoeffs.ShiftVectorCoeffs[2] = -0.00009509206560976;

    if (tmpCfg.scan_type == COLUMN_TYPE)
    {
        patDefCol patDefC;
        bool pack8 = false;
        int overlap = 0, maxOverlap = 0;    // amount of overlay between consecutive patterns

        // Find max shift amount in a 4 row section. The CCP block uses 8x4 blocks 
        shiftVector = (int8_t*)(malloc(sizeof(uint8_t) * DMD_HEIGHT));
        dlpspec_calib_genShiftVector(m_calibCoeffs.ShiftVectorCoeffs, DMD_HEIGHT, shiftVector);

        for (int k = 0; k < DMD_HEIGHT; k += 4)
        {
            for (int line = 1; line < 4; line++)
            {
                if (abs(shiftVector[line + k - 1] - shiftVector[line + k]) > blockShift)
                    blockShift = abs(shiftVector[line + k - 1] - shiftVector[line + k]);
            }
            if (blockShift > maxShift)
                maxShift = blockShift;
            blockShift = 0;
        }
        if (shiftVector != NULL)
            free(shiftVector);

        for (int i = 8; i <= (MAX_PATTERNS_PER_SCAN); i++)
        {
            tmpCfg.num_patterns = i;
            dlpspec_scan_col_genPatDef(&tmpCfg, &m_calibCoeffs, &patDefC);

            for (int j = 0; j < (patDefC.numPatterns - 8); j++)  // test pattern set with 8 members
            {
                overlap = patDefC.colWidth - abs(patDefC.colMidPix[j + 1] - patDefC.colMidPix[j]);
                if (overlap > maxOverlap)
                    maxOverlap = overlap;
                if (abs(patDefC.colMidPix[j + 7] - patDefC.colMidPix[j]) > (7 + maxShift + maxOverlap + 1))
                    pack8 = false;
                else
                {
                    pack8 = true;  // More than 8 colors between pattern midpoints
                    break;
                }
            }
            if ((i <= abs(patDefC.colMidPix[patDefC.numPatterns - 1] - patDefC.colMidPix[0])) && !pack8)
            {
                if ((pack8 && (patDefC.colWidth <= 16)) || !pack8)
                    maxResolutions = i;
                else
                    break;
            }
            else
                break;
        }
    }
    else if (tmpCfg.scan_type == HADAMARD_TYPE)
    {
        patDefHad patDefH;
        double startCol, endCol;

        dlpspec_util_nmToColumn(tmpCfg.wavelength_start_nm, m_calibCoeffs.PixelToWavelengthCoeffs, &endCol);
        dlpspec_util_nmToColumn(tmpCfg.wavelength_end_nm, m_calibCoeffs.PixelToWavelengthCoeffs, &startCol);

        for (int i = 1; i < MAX_PATTERNS_PER_SCAN; i++)
        {
            tmpCfg.num_patterns = i;
            ret_val = dlpspec_scan_had_genPatDef(&tmpCfg, &m_calibCoeffs, &patDefH);
            // valid pattern generation && unique non-repeated pattern && patterns do not exceed memory && patterns do not exceed ADC buffer
            if ((ret_val == DLPSPEC_PASS) && (i <= (endCol - startCol)) && (patDefH.numPatterns < MAX_PATTERNS_PER_SCAN) && (patDefH.numPatterns < (ADC_DATA_LEN - MAX_PATTERNS_PER_SCAN / 24)))
                maxResolutions = tmpCfg.num_patterns;
        }
    }
    return (int)maxResolutions * 0.95;
}
int ScanConfig_GetHadamardUsedPatterns(slewScanConfig* scanCfg, int section, char* SpectrumCalCoefficients)
{
    scanConfig tmpCfg;
    memcpy(&tmpCfg, &scanCfg->head, sizeof(scanCfg->head));
    tmpCfg.scan_type = scanCfg->section[section].section_scan_type;
    tmpCfg.wavelength_start_nm = scanCfg->section[section].wavelength_start_nm;
    tmpCfg.wavelength_end_nm = scanCfg->section[section].wavelength_end_nm;
    tmpCfg.width_px = scanCfg->section[section].width_px;
    tmpCfg.num_repeats = scanCfg->head.num_repeats;
    tmpCfg.num_patterns = scanCfg->section[section].num_patterns;

    if (tmpCfg.scan_type != HADAMARD_TYPE)
        return FAIL;

    calibCoeffs m_calibCoeffs;
    patDefHad patDefH;
    DLPSPEC_ERR_CODE ret_val = DLPSPEC_PASS;

    char byteData[sizeof(calibCoeffs) * 3] = "";
    memcpy(byteData, SpectrumCalCoefficients, sizeof(calibCoeffs) * 3);
    dlpspec_calib_read_data(&byteData, sizeof(calibCoeffs) * 3);
    memcpy(&m_calibCoeffs, &byteData, sizeof(calibCoeffs));

    ret_val = dlpspec_scan_had_genPatDef(&tmpCfg, &m_calibCoeffs, &patDefH);
    if (ret_val == DLPSPEC_PASS)
        return patDefH.numPatterns;
    else
        return FAIL;
}
