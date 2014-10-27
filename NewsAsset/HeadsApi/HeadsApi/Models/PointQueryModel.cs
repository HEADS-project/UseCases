using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace HeadsApi.Models
{
    public class PointQueryModel
    {
        public String Username;
        public double? Latitude;
        public double? Longitude;
        public double? Range;
        public long? From;
        public long? To;
        public String [] Keywords;
    }
}