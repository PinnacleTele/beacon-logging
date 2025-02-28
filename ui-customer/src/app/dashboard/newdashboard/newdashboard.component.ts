import { style } from '@angular/animations';
import { flatten } from '@angular/compiler';
import { Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import {
  ChartComponent,
  ApexNonAxisChartSeries,
  ApexAxisChartSeries,
  ApexChart,
  ApexFill,
  ApexYAxis,
  ApexTooltip,
  ApexMarkers,
  ApexXAxis,
  ApexPlotOptions,
  ApexLegend,
  ApexResponsive,
  ApexGrid,


} from "ng-apexcharts";
import {

  ApexDataLabels,
} from "ng-apexcharts";
import * as moment from "moment-timezone";
import { ApexOptions } from 'apexcharts';
import { DashboardService } from "src/app/dashboard/dashboard.service";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { HttpErrorResponse } from '@angular/common/http';

export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  xaxis: ApexXAxis;
  yaxis: ApexYAxis | ApexYAxis[];
  labels: string[];
  dataLabels: ApexDataLabels;
  stroke: any; // ApexStroke;
  markers: ApexMarkers;
  plotOptions: ApexPlotOptions;
  fill: ApexFill;
  tooltip: ApexTooltip;
  legend: ApexLegend;
  responsive: ApexResponsive[];
  grid: ApexGrid,


};

export type PieChartOptions = {
  series: ApexNonAxisChartSeries;
  chart: ApexChart;
  plotOptions: ApexPlotOptions;
  responsive: ApexResponsive[];
  labels: string[];
  datalabel: ApexDataLabels;
  fill: ApexFill;

};




@Component({
  selector: 'app-newdashboard',
  templateUrl: './newdashboard.component.html',
  styleUrls: ['./newdashboard.component.css']
})
export class NewdashboardComponent implements OnInit, OnDestroy {

  dstatLoader: boolean;

  dprocessLoader: boolean;

  mixedChartLoader: boolean;

  columnChartLoader: boolean;

  public statLoading = this.dservice.getStatsDataLoading.subscribe((data: any) => { this.dstatLoader = data });

  public processLoading = this.dservice.getProcessedDataLoading.subscribe((data: any) => { this.dprocessLoader = data });

  public mixLoading = this.dservice.getTrendDataLoading.subscribe((data: any) => { this.mixedChartLoader = data });

  public colLoading = this.dservice.dhTrendLoading.subscribe((data: any) => { this.columnChartLoader = data });


  pvalue: any = "this month";

  Tvalue: any = "this month";

  STATSList: any;

  processedData: any;

  dhTrendData: any[] = [];

  trendWiseData: any[] = [];

  columnDataXaxisLable: any[] = [];

  mixedChartXaxisLable: any[] = [];

  mixedChartData: any[] = [];

  totalDelivered: number = 0;

  totalSubmitted: number = 0;

  submittedColor: any;

  deliveredColor: any;


  DHsubmittedColor: any;

  daywiseApiError:boolean = false;

  hourlyTrend :boolean=false;

  progressApiError:boolean=false;

  DHdeliveredColor: any;


  DHTotalSubmit: any;

  DHTotaldeliver: any;

  @ViewChild("chart") chart: ChartComponent;

  interval: any;

  public mixedchartOptions: Partial<ChartOptions>;

  public columnchartOptions2: Partial<ChartOptions>;

  public piechartOptions: Partial<PieChartOptions>;

  mixedChartSort: any = 'month';

  mcTitle:string = "Daywise";

  dataModal = {
    stroke: ["smooth", "smooth", "straight"],
    fillType: ["solid", "solid", "solid"],
    opacity: [1, 1, 0],

    yaxisTickCount: 5,
    borderthickness: [1, 0, 3],
    markerSize: [0, 0, 1.5]
  }

  columndataModal = {
    colors: ['#6feded', '#6feded'],
    stroke: ["smooth", "smooth"],
    fillType: ["solid", "solid"],
    opacity: [5, 7],
    yaxisTickCount: 5,
    borderthickness: 4,
    markerSize: [0, 1],
    labelFontSize: "10",
    lableFontColor: "#4b4b4d"
  }

  piechartdataModal = {
    dataset:
    {
      name: "Success",
      displayName: "Success (Successfull upload)",
      type: "pie",
      color: "#0049f7",
      data: [10, 20, 50, 20]
    }
    ,
    colors: ['#6feded', '#6feded'],

    stroke: ["smooth", "smooth"],
    fillType: ["solid", "solid"],
    opacity: [5, 7],
    yAxis_label: [
      '00:00', '01:00', '02:00', '03:00', '04:00', '05:00',
      '06:00', '07:00', '08:00', '09:00', '10:00', '11:00',
      '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00', '24:00'
    ],
    display_value1: "success (succeeded)",
    display_value2: "In-Progress (Progressing)",
    yaxisTickCount: 3,
    borderthickness: 4,
    markerSize: [0, 1],
    labelFontSize: "10",
    lableFontColor: "#4b4b4d"
  }




  pieChartData = [
    {
      series: [10, 20, 20, 10, 40],
      labels: ["Value A", "Value B", "Value c", "Value D", "value E"],
      colors: ["#2140db", "#0afafa", "#0afa0a", "#dafa0a", "#fa2e0a"]
    }
  ]


  constructor(private dservice: DashboardService) {

    this.dservice.getStatsUrl("onload").subscribe((data: any) => {
      this.STATSList = data;
    })

    this.processDataSubscribe();

      this.hourlyTrendSubscribe();

      this.monthlyTrendSubscribe();

    // this.piechartOptions = {
    //   series: this.pieChartData[0].series,

    //   chart: {
    //     height: 380,
    //     type: "pie"
    //   },

    //   labels: this.pieChartData[0].labels,
    //   fill: {
    //     colors: this.pieChartData[0].colors,
    //   },
    //   plotOptions: {
    //     pie: {
    //       startAngle: 0,
    //       endAngle: 300,
    //       expandOnClick: true,
    //       customScale: 1,

    //       dataLabels: {

    //         offset: 0,
    //         minAngleToShowLabel: 10

    //       },


    //     }
    //   }
    // }
  }

  ngOnInit(): void {
  }

  // functions

  hourlyTrendSubscribe(){
      // 2nd chart
      this.dservice.getDHTrendData("onload").subscribe((data: any) => {
        this.hourlyTrend = false;
        //  data.dataset[1].type = "bar"
        this.DHTotalSubmit = data.total_submitted;
        this.DHTotaldeliver = data.total_delivered;
        this.DHdeliveredColor = data.dataset[1].color;
        this.DHsubmittedColor = data.dataset[0].color;
        this.dhTrendData = data.dataset;
        this.columnDataXaxisLable = data.xaxis_label
        this.columnchartOptions2 = {
          responsive: [{
            options: {
              chart: {
                height: 100,
                Animations:{
                  enabled: false,
                },
                animations: {
                  enabled: false,
                  animateGradually: {
                    enabled: false,
                  },
                  dynamicAnimation: {
                    enabled: false,
                  }
                }
              },
              plotOptions: {
                area: {
                  horizontal: true
                },
                line: {
                  horizontal: true
                }
              },
              legend: {
                position: "bottom"
              }
              ,
              label: {
  
                yaxis: false,
                xaxis: false
  
              }
            }
  
          }],
          // series:  this.dhTrendData,
  
          series: data.dataset,
  
          chart: {
            type: "line",
            width: "100%",
            height: 300,
            toolbar: {
              show: false,
            },
            zoom: {
              enabled: false
            },
            animations: {
              enabled: false,
              animateGradually: {
                enabled: false,
              },
              dynamicAnimation: {
                enabled: false,
              }
            }
          },
  
          stroke: {
            curve: this.columndataModal.stroke,
            width: this.columndataModal.borderthickness,
          },
          fill: {
            colors: ['#6feded', '#6feded'],
            opacity: this.columndataModal.opacity,
            type: this.columndataModal.fillType,
          },
          legend: {
            show: false
          },
          plotOptions: {
            bar: {
              borderRadius: 1.5,
              columnWidth: '50%',
              barHeight: '100%',
              distributed: false,
              rangeBarOverlap: true,
              rangeBarGroupRows: false,
              dataLabels: {
                position: 'top',
                maxItems: 100,
                hideOverflowingLabels: true,
              }
            }
          },
  
  
          xaxis: {
            categories: data.xaxis_label,
            labels: {
              show: true,
            }
  
          },
          grid: {
            show: false,
            borderColor: '#8cf584',
            strokeDashArray: 0,
            position: 'back',
          },
  
          yaxis: [
            {
              // tickAmount: this.columndataModal.yaxisTickCount,
              axisTicks: {
                show: false,
  
  
              },
              axisBorder: {
                show: true,
                color: '#4b4b4d',
  
  
              },
              labels: {
                style: {
                  fontSize: this.columndataModal.labelFontSize,
                  colors: this.columndataModal.lableFontColor,
  
                },
                formatter: function (y: any) {
                  if (y > 1000) {
                    if (y > 999 && y < 1000000) {
                      return parseFloat((y / 1000).toFixed(1)) + 'k';
                    } else if (y >= 1000000) {
                      return parseFloat((y / 1000000).toFixed(1)) + 'M';
                    } else if (y < 900) {
                      return y;
                    }
                  } else {
  
                    return y.toFixed(0);
                  }
                }
  
              },
  
              tooltip: {
                enabled: true,
  
              }
  
            },
  
          ],
          markers: {
            size: this.dataModal.markerSize
          },
          tooltip: {
            followCursor: true,
            intersect: false,
            shared: false,
            inverseOrder: false,
            onDatasetHover: { highlightDataSeries: true },
            x: {
              show: true,
              formatter: function (x) {
                if (typeof x !== "undefined") {
                  return x.toFixed(0) + " hour";
                }
                return x;
              }
            },
            y: {
              formatter: function numberWithCommas(y) {
                return y.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
              },
            }
          }
  
        };
      },
      (error:HttpErrorResponse)=>{
        this.hourlyTrend = true;
      })
  
  }

  monthlyTrendSubscribe(){
    this.dservice.getTrendWiseData(this.Tvalue, "onload").subscribe((data: any) => {
      this.daywiseApiError = false;
      this.totalDelivered = data.total_delivered;
      this.totalSubmitted = data.total_submitted;
      this.submittedColor = data.dataset[0].color;
      this.deliveredColor = data.dataset[1].color;

      let data1 = [];
      let data2 = [];
      let data3 = [];
      data.dataset[0].data.forEach(x => {
        x == null ? data1.push(0) : data1.push(x);

      });
      data.dataset[0].data = data1;


      data.dataset[1].data.forEach(x => {
        x == null ? data2.push(0) : data2.push(x);

      });
      data.dataset[1].data = data2;


      data.dataset[2].data.forEach(x => {
        x == null ? data3.push(0) : data3.push(x);

      });
      data.dataset[2].data = data3;
      this.mixedchartOptions = {
        responsive: [{
          options: {
            chart: {
              height: 300,
              width: '100%',
            },

            dataLabels: {
              style: {
                fontSize: '74',
                fontWeight: '600',
                colors: ['#000000', '#ffffff'],
              },
            },
            legend: {
              show: true,
              position: "bottom"
            },

          }

        },
        ],
        series: data.dataset,

        chart: {
          stacked: false,
          type: "line",
          height: '320',
          toolbar: {
            show: false,
          },
          zoom: {
            enabled: false
          },
          animations: {
            enabled: false,
            easing: 'easeinout',
            speed: 800,
            animateGradually: {
              enabled: false,
              delay: 150
            },
            dynamicAnimation: {
              enabled: false,
              speed: 350
            }
          }
        },
        stroke: {
          curve: this.dataModal.stroke,
          width: this.dataModal.borderthickness,
        },
        fill: {
          //  colors: this.dataModal.colors,
          opacity: this.dataModal.opacity,
          type: this.dataModal.fillType,
        },
        legend: {
          show: false
        },
        dataLabels: {
          enabled: true,
          style: {
            fontSize: '10px'
          },
          enabledOnSeries: [2],
          formatter: function (val: any) {
            // if (val != null ) {
            return val + '%';
            // }else{
            //   return   0 + '%';  
            // }
          },
        },
        xaxis: {
          type: 'category',
          categories: data.xaxis_label,

        },
        grid: {
          show: false,
          borderColor: '#8cf584',
          strokeDashArray: 0,
          position: 'back',
        },

        yaxis: [

          {
            // tickAmount: this.dataModal.yaxisTickCount,
            seriesName: data.dataset[0].name,
           // seriesName: "",
            // showForNullSeries: false,
            show: true,
            axisTicks: {
              show: true
            },
            axisBorder: {
              show: true,
              color: '#4b4b4d',
            },
            title: {

            },
            min: 0,
            max: undefined,
            labels: {
              style: {
                colors: "#4b4b4d"
              },
              formatter: function (y: any) {
                if (y > 1000) {
                  if (y > 999 && y < 1000000) {
                    return parseFloat((y / 1000).toFixed(1)) + 'k';
                  } else if (y >= 1000000) {
                    return parseFloat((y / 1000000).toFixed(1)) + 'M';
                  } else if (y < 900) {
                    return y;
                  }
                } else {

                  return y.toFixed(0);
                }
              }
            },
          },
         
         {
          // tickAmount: this.dataModal.yaxisTickCount,
          seriesName: data.dataset[0].name,
         // seriesName: "",
          // showForNullSeries: false,
          show: false,
          axisTicks: {
            show: true
          },
          axisBorder: {
            show: true,
            color: '#4b4b4d',
          },
          title: {
          },
          min: 0,
          max: undefined,
          labels: {
            style: {
              colors: "#4b4b4d"
            },
            formatter: function (y: any) {
              if (y > 1000) {
                if (y > 999 && y < 1000000) {
                  return parseFloat((y / 1000).toFixed(1)) + 'k';
                } else if (y >= 1000000) {
                  return parseFloat((y / 1000000).toFixed(1)) + 'M';
                } else if (y < 900) {
                  return y;
                }
              } else {

                return y.toFixed(0);
              }
            }
          }

        },

          {
            seriesName: data.dataset[2].name,
            show: true,
            opposite: true,
            // showForNullSeries: true,
            axisTicks: {
              show: true
            },
            axisBorder: {
              show: true,
              color: "#4b4b4d"
            },
            title: {
              // text: data.dataset[2].name,
              text: 'Delivery %',
              style: {
                fontWeight: 30,
                color: "#4b4b4d"
              }
            },
            min: 0,
            max: 100,
            labels: {

              formatter: function (y: any) {
                if (y != null) {
                  return y + '%';
                } else {
                  return 0 + '%';
                }
                //  return y.toFixed(0) + "%";

              }
            },


          }
        ],
        markers: {

           size: 4,
          //  colors: ['#a2c1f2'],

          discrete: [],
          shape: "circle",
          height: 7,
          width: 25,
          radius: 0,
          offsetX: 0,
          offsetY: 0,
          onClick: undefined,
          onDblClick: undefined,
          showNullDataPoints: true,
          hover: {
            size: undefined,
            sizeOffset: 3
          }
        },
        tooltip: {
          followCursor: true,
          intersect: false,
          shared: false,
          inverseOrder: false,
          onDatasetHover: { highlightDataSeries: true },
          x: {
            show: false
          },
          y: [
            {
              formatter: function numberWithCommas(y) {
                return y.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
              }
            },
            {
              formatter: function numberWithCommas(y) {
                return y.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
              }
            },
            {
              formatter: function (y: any) {
                if (y == null) {
                  return 0 + '%';
                } else {
                  return y + '%';
                }

              }
            }
          ],
        }
      };
      this.threadProcess();
    },
    (error:HttpErrorResponse)=>{
      this.daywiseApiError = true;
    })
  }

  processDataSubscribe() {
   
    this.dservice.getProcessedUrl(this.pvalue, "onload").subscribe((data: any) => {
      this.progressApiError = false;
      this.processedData = data;
    },(error:HttpErrorResponse)=>{
      this.progressApiError = true;
    })
  }

  
  
  year(value) {
    this.mixedChartSort = value;
    if (value == 'month') {
      this.Tvalue = "this month";
     this.mcTitle = "Daywise";

      this.trendWiseDataSubscribe();
    } else if (value == 'year') {
      this.Tvalue = "this year";
      this.mcTitle = "Monthwise";
      this.trendWiseDataSubscribe();
    }
  }

  trendWiseDataSubscribe() {
    this.dservice.getTrendWiseData(this.Tvalue, "onload").subscribe((data: any) => {
      this.totalDelivered = data.total_delivered;
      this.totalSubmitted = data.total_submitted;
      let data1 = [];
      data.dataset[0].data.forEach(x => {
        x == null ? data1.push(0) : data1.push(x);

      });
      data.dataset[0].data = data1;

      let data2 = [];
      data.dataset[1].data.forEach(x => {
        x == null ? data2.push(0) : data2.push(x);

      });
      data.dataset[1].data = data2;

      let data3 = [];
      data.dataset[2].data.forEach(x => {
        x == null ? data3.push(0) : data3.push(x);

      });
      data.dataset[2].data = data3;

      this.mixedchartOptions.series = data.dataset;
      this.mixedchartOptions.xaxis = { categories: data.xaxis_label };

    })
  }

 

  percentageConvertor(num: string) {

    if (num == null || num == undefined || num == "NaN") {
      return 0 + "%";
    } else {
      return num + "%";

    }

  }


  nullHandler(num: string) {

    if (num == null || num == undefined || num == 'NaN') {
      return 0;
    } else {
      return num;

    }

  }

  processStatsFiltr(value) {
    if (value == 'today') {
      this.pvalue = value;
      this.processDataSubscribe();
    } else if (value == 'this month') {
      this.pvalue = value;
      this.processDataSubscribe();
    }
  }

  threadProcess() {
    this.interval = setInterval(() => {
      this.dservice.getTrendWiseData(this.Tvalue, "thread").subscribe((data: any) => {
        this.totalDelivered = data.total_delivered;
        this.totalSubmitted = data.total_submitted;


        let data1 = [];
        data.dataset[0].data.forEach(x => {
          x == null ? data1.push(0) : data1.push(x);

        });
        data.dataset[0].data = data1;

        let data2 = [];
        data.dataset[1].data.forEach(x => {
          x == null ? data2.push(0) : data2.push(x);

        });
        data.dataset[1].data = data2;

        let data3 = [];
        data.dataset[2].data.forEach(x => {
          x == null ? data3.push(0) : data3.push(x);

        });
        data.dataset[2].data = data3;
        // data.dataset[0].data = [ 
        //   10,
        //   40,
        //  50,
        //   40,
        //   180,
        //   99,
        //   60,
        //   230,
        //   150,
        //   100,
        //   30,
        //   10,
        //  ]
        //  data.dataset[1].data = [ 
        //   10,
        //   40,
        //  70,
        //   80,
        //   100,
        //   99,
        //   50,
        //   70,
        //   20,
        //   40,
        //   10,
        //   50,
        //  ]
        //  data.dataset[2].data = [ 
        //   40,
        //   30,
        //  50,
        //   10,
        //   30,
        //   99,
        //   80,
        //   50,
        //   100,
        //   70,
        //   100,
        //   40,
        //  ]
        this.mixedchartOptions.series = data.dataset;
        // data.dataset[1].color = "#c74822"
        this.mixedchartOptions.xaxis = { categories: data.xaxis_label };

      }),
        this.dservice.getDHTrendData("thread").subscribe((data: any) => {
          this.DHTotalSubmit = data.total_submitted;
          this.DHTotaldeliver = data.total_delivered;
          this.DHdeliveredColor = data.dataset[1].color;
          this.DHsubmittedColor = data.dataset[0].color;
          this.columnchartOptions2.series = data.dataset;
          this.columnchartOptions2.xaxis = { categories: data.xaxis_label };
          // this.columnDataXaxisLable = data.xaxis_label
        }),
        this.dservice.getStatsUrl("thread").subscribe((data: any) => {
          this.STATSList = data;
        }),
        this.dservice.getProcessedUrl(this.pvalue, "thread").subscribe((data: any) => {
          this.processedData = data;
        })

    }, CONSTANTS.apiHitTimer)

  }

  retry(){

  }

  ngOnDestroy() {
    if (this.interval) {
      clearInterval(this.interval);
    }
  }
}
