using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace HeadsApi.Models
{
    public class PointModel
    {
        public int PointId { get; set; }
        public string Title { get; set; }
        public string Description { get; set; }
        public decimal Latitude { get; set; }
        public decimal Longitude { get; set; }
        public long Uploaded { get; set; }
        public string Username { get; set; }
    }
}