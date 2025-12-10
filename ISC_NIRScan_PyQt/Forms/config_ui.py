# -*- coding: utf-8 -*-
"""
    This file is part of ISC NIRScan GUI SDK Python.

    ISC NIRScan GUI SDK Python is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    ISC NIRScan GUI SDK Python is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
"""
from PyQt5 import QtWidgets
from PyQt5 import QtCore

# UI modules
from .scanconfigdialog import Ui_ScanConfigDialog


class ConfigUI(QtWidgets.QDialog, Ui_ScanConfigDialog):
    def __init__(self, device, cfglist):
        QtWidgets.QDialog.__init__(self)
        self.setupUi(self)

        if device.DevInfo['MaxWavelength'] == 2400:
            self.PIXEL_WIDTH = 1
        else:
            self.PIXEL_WIDTH = (device.DevInfo['MaxWavelength'] - device.DevInfo['MinWavelength']) / (854 * 0.8)
        self.NUM_WIDTH_ITEMS = 60
        self.DEFAULT_WIDTH_INDEX = 5
        self.MIN_PIXEL_INDEX = 2
        self.MIN_PATTERNS = 3
        self.MAX_PATTERNS_PER_SCAN = 624
        self.cfglist = cfglist
        self.device = device
        self.edit_mode = -1
        self.listRow = -1
        self.errMessage = ""
        self.init_ui()

    def init_ui(self):
        print('Config Dialog init')
        self.pushButton_add_config.clicked.connect(self.pushButton_add_config_onClick)
        self.pushButton_edit_config.clicked.connect(self.pushButton_edit_config_onClick)
        self.pushButton_delete_config.clicked.connect(self.pushButton_delete_config_onClick)
        self.pushButton_set_active.clicked.connect(self.pushButton_set_active_onClick)
        self.listWidget_target_scan_cfg.clicked.connect(self.listWidget_target_scan_cfg_onClick)
        self.spinBox_numSections.valueChanged.connect(self.spinBox_numSections_valueChanged)
        self.pushButton_scan_config_save.clicked.connect(self.pushButton_scan_config_save_onClick)
        self.buttonBox.accepted.connect(self.buttonBox_accept)
        self.buttonBox.rejected.connect(self.buttonBox_reject)

        self.label_cfglist_activeCfg.setText(self.cfglist.TargetConfig[self.cfglist.ActCfgIdx][2])
        self._clear_editBoxes()
        self._populate_deviceCfgs()
        self.groupBox_ScanConfig.setEnabled(False)
        self.SlewItemsWidget.horizontalHeader().setSectionResizeMode(QtWidgets.QHeaderView.Stretch)

    def pushButton_scan_config_save_onClick(self):
        ret = -1
        totalresolutions = 0
        if self.edit_mode == 0:
            new_config = self._construct_config()
            if not isinstance(new_config, tuple):
                self.ErrorMsg(self.errMessage)
                return ret
            for i in range(self.spinBox_numSections.value()):
                maxresolution = self.cfglist.GetMaxResolution(new_config, i)
                if maxresolution < 0:
                    self.ErrorMsg("Max pattern fail")
                    return ret
                elif new_config[5][i][4] > maxresolution:
                    self.ErrorMsg("The maximum resolution possible for the section " + str(i + 1) + " is " + str(maxresolution))
                    return ret
                else:
                    totalresolutions += maxresolution
            if totalresolutions > self.MAX_PATTERNS_PER_SCAN:
                self.ErrorMsg("Total number of resolutions " + str(totalresolutions) + " exceeds " + str(self.MAX_PATTERNS_PER_SCAN))
                return ret
            ret = self.cfglist.AddIntoTargetConfigList(new_config)
            if ret < 0:
                self.ErrorMsg("Failed to add new config")
        elif self.edit_mode == 1:
            index = self.listRow
            if index < 0:
                self.ErrorMsg("Item not Selected")
                return ret
            edit_config = self._construct_config()
            if not isinstance(edit_config, tuple):
                self.ErrorMsg(self.errMessage)
                return ret
            for i in range(self.spinBox_numSections.value()):
                maxresolution = self.cfglist.GetMaxResolution(edit_config, i)
                if maxresolution < 0:
                    self.ErrorMsg("Max pattern fail")
                    return ret
                elif edit_config[5][i][4] > maxresolution:
                    self.ErrorMsg("The maximum resolution possible for the section " + str(i + 1) + " is " + str(maxresolution))
                    return ret
                else:
                    totalresolutions += maxresolution
            if totalresolutions > self.MAX_PATTERNS_PER_SCAN:
                self.ErrorMsg("Total number of resolutions " + str(totalresolutions) + " exceeds " + str(self.MAX_PATTERNS_PER_SCAN))
                return ret
            ret = self.cfglist.EditTargetConfigList(edit_config, index)
            if ret < 0:
                return ret
        elif self.edit_mode == 2:
            print('delete')
        else:
            self.ErrorMsg("Command not found")
            return ret

        self.edit_mode = -1
        self.listRow = -1
        self._clear_editBoxes()
        self.groupBox_ScanConfig.setEnabled(False)
        self._populate_deviceCfgs()

    def pushButton_add_config_onClick(self):
        if self.cfglist.TargetConfigLen >= 20:
            self.ErrorMsg("Cannot add over 20 scan configs to device." + self.cfglist.errMessage)
            return
        self.groupBox_ScanConfig.setEnabled(True)
        self._clear_editBoxes()
        self.spinBox_numSections.setValue(1)
        self.spinBox_num_scans_avg.setValue(6)
        self._populate_configGrid(1)
        self.edit_mode = 0

    def pushButton_edit_config_onClick(self):
        self.groupBox_ScanConfig.setEnabled(True)
        self.edit_mode = 1

    def pushButton_delete_config_onClick(self):
        ret = -1
        self.edit_mode = 2
        index = self.listRow
        if index < 0:
            self.ErrorMsg("Item not Selected")
            return ret
        ret = self.cfglist.DeleteConfig(index)
        if ret < 0:
            self.ErrorMsg('Failed to Delete Config')
        self._populate_deviceCfgs()

    def pushButton_set_active_onClick(self):
        ret = -1
        index = self.listRow
        if index < 0:
            self.ErrorMsg("Item not Selected")
            return ret
        ret = self.cfglist.SetTargetActiveScanIndex(index)
        if ret >= 0:
            index = self.cfglist.GetTargetActiveScanIndex()
            if index >= 0:
                self.label_cfglist_activeCfg.setText(self.cfglist.TargetConfig[self.cfglist.ActCfgIdx][2])
            else:
                self.ErrorMsg("Failed to read device active config")
        else:
            self.ErrorMsg(self.cfglist.errMessage)

    def listWidget_target_scan_cfg_onClick(self):
        self.listRow = self.listWidget_target_scan_cfg.currentRow()
        if 0 <= self.listRow < self.cfglist.TargetConfigLen:
            self._display_config_item()

    def spinBox_numSections_valueChanged(self):
        edit_section = self.spinBox_numSections.value()
        self._populate_configGrid(edit_section)

    def buttonBox_accept(self):
        print("OK")
        ret = self.cfglist.SetConfigList(self.cfglist.TargetConfig)
        if ret < 0:
            self.ErrorMsg("Failed to save configs to device.")

    def buttonBox_reject(self):
        print("Cancel")

    # Call by class
    def _populate_deviceCfgs(self):
        self.listWidget_target_scan_cfg.clear()
        for i in range(self.cfglist.TargetConfigLen):
            self.listWidget_target_scan_cfg.addItem(self.cfglist.TargetConfig[i][2])
        # print('actived idx: ', self.cfglist.ActCfgIdx)
        self.listRow = self.cfglist.ActCfgIdx
        self.listWidget_target_scan_cfg.setCurrentRow(self.listRow)
        self.SlewItemsWidget.setRowCount(0)
        self._display_config_item()

    def _display_config_item(self):
        currentRow = self.listWidget_target_scan_cfg.currentRow()
        uConfig = self.cfglist.TargetConfig[currentRow]
        self.lineEdit_scan_config_name.setText(uConfig[2])
        self.spinBox_num_scans_avg.setValue(uConfig[3])
        self.spinBox_numSections.setValue(uConfig[4])
        section_num = uConfig[4]
        self._populate_configGrid(section_num)

        for i in range(uConfig[4]):
            cb_type = self.SlewItemsWidget.cellWidget(i, 0)
            if cb_type:
                cb_type.setCurrentIndex(uConfig[5][i][0])
            le_startnm = self.SlewItemsWidget.cellWidget(i, 1)
            if le_startnm:
                le_startnm.setText(str(uConfig[5][i][2]))
            le_endnm = self.SlewItemsWidget.cellWidget(i, 2)
            if le_endnm:
                le_endnm.setText(str(uConfig[5][i][3]))
            cb_width = self.SlewItemsWidget.cellWidget(i, 3)
            if cb_width:
                pixelText = "{:.2f}".format(round(uConfig[5][i][1] * self.PIXEL_WIDTH, 2))
                index = cb_width.findText(pixelText)
                cb_width.setCurrentIndex(index)
            cb_exp = self.SlewItemsWidget.cellWidget(i, 4)
            if cb_exp:
                cb_exp.setCurrentIndex(uConfig[5][i][5])
            le_patterns = self.SlewItemsWidget.cellWidget(i, 5)
            if le_patterns:
                le_patterns.setText(str(uConfig[5][i][4]))

    def _populate_configGrid(self, section_num):
        row_counter = self.SlewItemsWidget.rowCount()
        if row_counter < 0:
            row_counter = 0
        self.SlewItemsWidget.setRowCount(section_num)
        for i in range(row_counter, section_num):
            cb_type = QtWidgets.QComboBox()
            cb_type.addItem("Column")
            cb_type.addItem("Hadamard")
            self.SlewItemsWidget.setCellWidget(i, 0, cb_type)

            le_startnm = QtWidgets.QLineEdit()
            self.SlewItemsWidget.setCellWidget(i, 1, le_startnm)

            le_endnm = QtWidgets.QLineEdit()
            self.SlewItemsWidget.setCellWidget(i, 2, le_endnm)

            cb_width = QtWidgets.QComboBox()
            nmItem = self.PIXEL_WIDTH
            j = self.MIN_PIXEL_INDEX
            while nmItem < self.NUM_WIDTH_ITEMS:
                nmItem = j * self.PIXEL_WIDTH
                item = "{:.2f}".format(nmItem)
                cb_width.addItem(item)
                j += 1
            cb_width.setCurrentIndex(self.DEFAULT_WIDTH_INDEX)
            self.SlewItemsWidget.setCellWidget(i, 3, cb_width)

            cb_exp = QtWidgets.QComboBox()
            cb_exp.addItem("0.635")
            cb_exp.addItem("1.270")
            cb_exp.addItem("2.540")
            cb_exp.addItem("5.080")
            cb_exp.addItem("15.240")
            cb_exp.addItem("30.480")
            cb_exp.addItem("60.960")
            self.SlewItemsWidget.setCellWidget(i, 4, cb_exp)

            le_patterns = QtWidgets.QLineEdit()
            self.SlewItemsWidget.setCellWidget(i, 5, le_patterns)

    def _clear_editBoxes(self):
        self.spinBox_num_scans_avg.setValue(1)
        self.spinBox_num_scans_avg.clear()
        self.lineEdit_scan_config_name.clear()
        self.spinBox_numSections.setValue(1)
        self.spinBox_numSections.clear()
        self.SlewItemsWidget.setRowCount(0)

    def _construct_config(self):
        ret = -1
        cfg_name = self.lineEdit_scan_config_name.text()
        if cfg_name == "":
            self.errMessage = "Empty config name"
            return ret
        cfg_repeat = self.spinBox_num_scans_avg.value()
        cfg_sectionNum = self.spinBox_numSections.value()
        scan_type = -1
        startnm = -1
        endnm = -1
        width = -1
        exp = -1
        patterns = -1
        cfg_sections = []
        for i in range(cfg_sectionNum):
            cb_type = self.SlewItemsWidget.cellWidget(i, 0)
            if cb_type:
                scan_type = cb_type.currentIndex()
            le_startnm = self.SlewItemsWidget.cellWidget(i, 1)
            if le_startnm:
                startnm = le_startnm.text()
                startnm = int(startnm)
            le_endnm = self.SlewItemsWidget.cellWidget(i, 2)
            if le_endnm:
                endnm = le_endnm.text()
                endnm = int(endnm)
            cb_width = self.SlewItemsWidget.cellWidget(i, 3)
            if cb_width:
                width_nm = float(cb_width.currentText())
                width = int(width_nm / self.PIXEL_WIDTH)
            cb_exp = self.SlewItemsWidget.cellWidget(i, 4)
            if cb_exp:
                exp = cb_exp.currentIndex()
            le_patterns = self.SlewItemsWidget.cellWidget(i, 5)
            if le_patterns:
                patterns = le_patterns.text()
                if patterns == "":
                    patterns = self.MIN_PATTERNS
                patterns = int(patterns)

            if startnm < self.device.DevInfo['MinWavelength'] or endnm > self.device.DevInfo['MaxWavelength'] or \
                    startnm >= endnm:
                self.errMessage = "Please enter values in the range of " + str(self.device.DevInfo['MinWavelength']) + \
                                  " to " + str(self.device.DevInfo['MaxWavelength'] + " for section " + str(i + 1))
                return ret

            section = (scan_type, width, startnm, endnm, patterns, exp)
            cfg_sections.append(section)

        new_config = (2, self.device.serialNumber, cfg_name, cfg_repeat, cfg_sectionNum, cfg_sections)
        return new_config

    def ErrorMsg(self, msg):
        msgBox = QtWidgets.QMessageBox()
        msgBox.setIcon(QtWidgets.QMessageBox.Warning)
        msgBox.setText(msg)
        msgBox.exec_()
